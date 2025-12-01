package com.internal.feature.aml.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.internal.enumation.AmlStatusEnum;
import com.internal.exceptions.error.custom.NotFoundException;
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
import com.internal.feature.logs_report.service.AccountOnlineOpenFinalService;
import com.internal.feature.master_data.dto.response.LocationCodesDto;
import com.internal.feature.open_account.mapper.MasterDataServiceHelper;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private final AccountOnlineOpenFinalService accountOnlineOpenFinalService;
    private final OpenAccountTelegramAlertServiceImpl alertTelegramService;
    private final MasterDataServiceHelper masterDataServiceHelper;

    // ------------------------------- FIND BY LEGAL ID -------------------------------
    @Override
    public Optional<AmlStatus> findByLegalId(String legalId) {
        return amlStatusRepository.findByLegalId(legalId);
    }

    // ------------------------------- CREATE AML STATUS -------------------------------
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AmlStatus createAmlStatus(CreateAmlRequestDto requestDto) throws JsonProcessingException {
        AmlStatus status = amlStatusMapper.fromCreateDto(requestDto);
        status.setRejectedBy(null);

        // Populate address fields using code-based lookup
        populateAddressFields(status, requestDto);

        // Save AML record
        status = amlStatusRepository.save(status);

        // Create initial history (PENDING)
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, null);

        amlHistoryRepository.save(history);

        return status;
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

        if(req.getRemark() != null) {
            status.setRemarks(req.getRemark());
        }

        // Save & record history
        status = amlStatusRepository.save(status);
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, currentUser);
        amlHistoryRepository.save(history);

        // Convert to DTO
        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status);

        // Update the final log table
        accountOnlineOpenFinalService.updateFinalLogWithAml(amlDto);

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
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<AmlStatus> spec = AmlStatusSpecification.hasStatus(request.getStatus())
                .and(AmlStatusSpecification.search(request.getSearch()));

        Page<AmlStatus> page = amlStatusRepository.findAll(spec, pageable);

        List<AmlStatusDto> content = page.stream()
                .map(amlStatusMapper::toStatusDto)
                .collect(Collectors.toList());

        return amlStatusMapper.mapToListDto(content, page);
    }

    @Override
    public AmlStatusDto getAmlById(Long id) {
        AmlStatus aml = amlStatusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aml not found with Id: " + id));
        return amlStatusMapper.toStatusDto(aml);
    }

    @Override
    public AmlHistoryDto getAmlHistoryById(Long id) {
        AmlHistory aml = amlHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aml history not found with Id: " + id));
        return amlHistoryMapper.toDto(aml);
    }

    // ------------------------------- GET ALL AML HISTORY -------------------------------
    @Override
    public AllAmlHistoryResponseDto getAllAmlHistory(AllAmlHistoryRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<AmlHistory> spec = AmlHistorySpecification.createdBetween(request.getStartDate(), request.getEndDate())
                .and(AmlHistorySpecification.search(request.getSearch()))
                .and(AmlHistorySpecification.hasStatus(request.getStatus()));

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
            LocationCodesDto currentAddr = masterDataServiceHelper.resolveCurrentAddress(requestDto);
            status.setCurrentAddressName(masterDataServiceHelper.buildFullAddressName(currentAddr));
            status.setCurrentAddressCode(masterDataServiceHelper.buildFullAddressCode(currentAddr));
        }

        // ------------------ Place of Birth ------------------
        if (requestDto.getCustomerPobProvince() != null && !requestDto.getCustomerPobProvince().isEmpty()) {
            LocationCodesDto pobAddr = masterDataServiceHelper.resolvePlaceOfBirth(requestDto);
            status.setPlaceOfBirthName(masterDataServiceHelper.buildFullAddressName(pobAddr));
            status.setPlaceOfBirthCode(masterDataServiceHelper.buildFullAddressCode(pobAddr));
        }
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
