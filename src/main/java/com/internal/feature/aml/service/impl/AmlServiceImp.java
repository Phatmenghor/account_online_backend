package com.internal.feature.aml.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.AllAmlHistoryRequestDto;
import com.internal.feature.aml.dto.request.AllAmlRequestDto;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.UpdateAmlStatusDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlHistoryDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.mapper.AmlHistoryMapper;
import com.internal.feature.aml.mapper.AmlStatusMapper;
import com.internal.feature.aml.model.AmlHistory;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.aml.repository.AmlHistoryRepository;
import com.internal.feature.aml.repository.AmlStatusRepository;
import com.internal.feature.aml.service.AmlService;
import com.internal.feature.aml.specification.AmlHistorySpecification;
import com.internal.feature.aml.specification.AmlStatusSpecification;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.master_data.dto.response.LocationCodesDto;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmlServiceImp implements AmlService {

    private final AmlStatusRepository amlStatusRepository;
    private final AmlHistoryRepository amlHistoryRepository;
    private final AmlStatusMapper amlStatusMapper;
    private final AmlHistoryMapper amlHistoryMapper;
    private final SecurityUtils securityUtils;
    private final OpenAccountTelegramAlertServiceImpl alertTelegramService;
    private final MasterDataService masterDataService;

    // ------------------------------- FIND BY LEGAL ID -------------------------------
    @Override
    public Optional<AmlStatus> findByLegalId(String legalId) {
        return amlStatusRepository.findByLegalId(legalId);
    }

    // ------------------------------- CREATE AML STATUS -------------------------------
    @Override
    @Transactional
    public AmlStatusDto createAmlStatus(CreateAmlRequestDto requestDto) throws JsonProcessingException {
        AmlStatus status = amlStatusMapper.fromCreateDto(requestDto);
        status.setRejectedBy(null);

        // Populate address fields using code-based lookup
        populateAddressFields(status, requestDto);

        // Save AML record
        status = amlStatusRepository.save(status);

        // Create initial history (PENDING)
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, null);
        amlHistoryRepository.save(history);

        // Prepare DTO
        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status);

//        // Telegram notification
        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send PENDING AML Telegram notification: {}", e.getMessage());
        }

        return amlDto;
    }

    // ------------------------------- UPDATE AML STATUS -------------------------------
    @Override
    @Transactional
    public AmlStatusDto updateAmlStatus(Long id, UpdateAmlStatusDto req) throws JsonProcessingException {
        UserEntity currentUser = securityUtils.getCurrentUser();

        AmlStatus status = amlStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AML Status not found"));

        // Update based on new status
        updateStatusByEnum(status, req.getStatus(), currentUser);

        // Save & record history
        status = amlStatusRepository.save(status);
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, currentUser);
        amlHistoryRepository.save(history);

        // Convert to DTO
        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status);

        // Telegram notification
        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send AML {} Telegram notification: {}", req.getStatus(), e.getMessage());
        }

        return amlDto;
    }

    // ------------------------------- GET ALL AML STATUS -------------------------------
    @Override
    public AllAmlResponseDto getAllAml(AllAmlRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<AmlStatus> spec = AmlStatusSpecification.hasStatus(request.getStatus())
                .and(AmlStatusSpecification.search(request.getSearch()));

        Page<AmlStatus> page = amlStatusRepository.findAll(spec, pageable);

        List<AmlStatusDto> content = page.stream()
                .map(amlStatusMapper::toStatusDto)
                .collect(Collectors.toList());

        return amlStatusMapper.mapToListDto(content, page);
    }

    // ------------------------------- GET ALL AML HISTORY -------------------------------
    @Override
    public AllAmlHistoryResponseDto getAllAmlHistory(AllAmlHistoryRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<AmlHistory> spec = AmlHistorySpecification.createdBetween(request.getStartDate(), request.getEndDate())
                .and(AmlHistorySpecification.search(request.getSearch()));

        Page<AmlHistory> page = amlHistoryRepository.findAll(spec, pageable);

        List<AmlHistoryDto> content = page.stream()
                .map(amlHistoryMapper::toDto)
                .collect(Collectors.toList());

        return amlHistoryMapper.mapToListDto(content, page);
    }

    // ------------------------------- PRIVATE METHODS -------------------------------

    private void populateAddressFields(AmlStatus status, CreateAmlRequestDto requestDto) {

        // ------------------ Current Address ------------------
        if (requestDto.getCustomerCurrentProvince() != null && !requestDto.getCustomerCurrentProvince().isEmpty()) {
            LocationCodesDto currentAddr = LocationCodesDto.builder()
                    .province(masterDataService.getProvinceByCode(requestDto.getCustomerCurrentProvince()))
                    .district(masterDataService.getDistrictByCode(requestDto.getCustomerCurrentDistrict()))
                    .commune(masterDataService.getCommuneByCode(requestDto.getCustomerCurrentCommune()))
                    .village(masterDataService.getVillageByCode(requestDto.getCustomerCurrentVillage()))
                    .build();

            status.setCurrentAddressName(buildFullAddressName(currentAddr));
            status.setCurrentAddressCode(buildFullAddressCode(currentAddr));
        }

        // ------------------ Place of Birth ------------------
        if (requestDto.getCustomerPobProvince() != null && !requestDto.getCustomerPobProvince().isEmpty()) {
            LocationCodesDto pobAddr = LocationCodesDto.builder()
                    .province(masterDataService.getProvinceByCode(requestDto.getCustomerPobProvince()))
                    .district(masterDataService.getDistrictByCode(requestDto.getCustomerPobDistrict()))
                    .commune(masterDataService.getCommuneByCode(requestDto.getCustomerPobCommune()))
                    .village(masterDataService.getVillageByCode(requestDto.getCustomerPobVillage())) // optional
                    .build();

            status.setPlaceOfBirthName(buildFullAddressName(pobAddr));
            status.setPlaceOfBirthCode(buildFullAddressCode(pobAddr));
        }
    }

    private String buildFullAddressName(LocationCodesDto loc) {
        StringBuilder sb = new StringBuilder();
        if (loc.getProvince() != null)
            sb.append(loc.getProvince().getProvinceEn()).append(" (").append(loc.getProvince().getProvinceKh()).append(")");
        if (loc.getDistrict() != null)
            sb.append(", ").append(loc.getDistrict().getDistrictEn()).append(" (").append(loc.getDistrict().getDistrictKh()).append(")");
        if (loc.getCommune() != null)
            sb.append(", ").append(loc.getCommune().getCommuneEn()).append(" (").append(loc.getCommune().getCommuneKh()).append(")");
        if (loc.getVillage() != null)
            sb.append(", ").append(loc.getVillage().getVillageEn()).append(" (").append(loc.getVillage().getVillageKh()).append(")");
        return sb.toString();
    }

    private String buildFullAddressCode(LocationCodesDto loc) {
        List<String> codes = new ArrayList<>();
        if (loc.getProvince() != null) codes.add(loc.getProvince().getProvinceCode());
        if (loc.getDistrict() != null) codes.add(loc.getDistrict().getDistrictCode());
        if (loc.getCommune() != null) codes.add(loc.getCommune().getCommuneCode());
        if (loc.getVillage() != null) codes.add(loc.getVillage().getVillageCode());
        return String.join(",", codes);
    }

    private void updateStatusByEnum(AmlStatus status, AmlStatusEnum newStatus, UserEntity user) {
        status.setStatus(newStatus);
        switch (newStatus) {
            case APPROVE:
                status.setApprovedBy(user);
                status.setRejectedBy(null);
                break;
            case REJECT:
                status.setRejectedBy(user);
                status.setApprovedBy(null);
                break;
            default:
                status.setApprovedBy(null);
                status.setRejectedBy(null);
                break;
        }
    }
}
