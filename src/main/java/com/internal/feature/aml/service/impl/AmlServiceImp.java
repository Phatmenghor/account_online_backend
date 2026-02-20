package com.internal.feature.aml.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.internal.enumation.AmlStatusEnum;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.aml.dto.request.AllAmlHistoryRequestDto;
import com.internal.feature.aml.dto.request.AllAmlRequestDto;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.ExternalAmlStatusUpdateDto;
import com.internal.feature.aml.dto.request.UpdateAmlStatusDto;
import com.internal.feature.aml.dto.response.AllAmlHistoryResponseDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlHistoryDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.event.AmlStatusChangedEvent;
import com.internal.feature.aml.mapper.AmlHistoryMapper;
import com.internal.feature.aml.mapper.AmlStatusMapper;
import com.internal.feature.aml.model.AmlHistory;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.aml.repository.AmlHistoryRepository;
import com.internal.feature.aml.repository.AmlStatusRepository;
import com.internal.feature.aml.service.AmlService;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.master_data.dto.response.LocationCodesDto;
import com.internal.feature.open_account.mapper.MasterDataServiceHelper;
import com.internal.utils.SecurityUtils;
import com.internal.utils.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ApplicationEventPublisher eventPublisher;
    private final MasterDataServiceHelper masterDataServiceHelper;

    @Override
    public Optional<AmlStatus> findByLegalId(String legalId) {
        log.debug("Finding AML status by legalId: {}", legalId);
        return amlStatusRepository.findByLegalId(legalId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AmlStatus createAmlStatus(CreateAmlRequestDto requestDto) throws JsonProcessingException {
        Optional<AmlStatus> existingOpt = amlStatusRepository.findByLegalId(requestDto.getLegalId());

        AmlStatus status;
        if (existingOpt.isPresent()) {
            log.info("AML status already exists for legalId: {}, updating existing record to PENDING", requestDto.getLegalId());
            status = existingOpt.get();
            amlStatusMapper.updateFromCreateDto(requestDto, status);
            status.setApprovedBy(null);
            status.setRejectedBy(null);
        } else {
            log.info("Creating new AML status for legalId: {}", requestDto.getLegalId());
            status = amlStatusMapper.fromCreateDto(requestDto);
            status.setRejectedBy(null);
        }

        // Populate address fields using code-based lookup
        populateAddressFields(status, requestDto);

        // Save AML record
        status = amlStatusRepository.save(status);

        // Create history entry (PENDING)
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, null);
        amlHistoryRepository.save(history);

        return status;
    }

    @Override
    @Transactional
    public AmlStatusDto updateAmlStatus(Long id, UpdateAmlStatusDto req) {
        log.info("Updating AML status with id: {} to status: {}", id, req.getStatus());
        UserEntity currentUser = securityUtils.getCurrentUser();

        AmlStatus status = amlStatusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AML Status not found"));

        // Update based on new status
        updateStatusByEnum(status, req.getStatus(), currentUser);

        if (req.getRemark() != null) {
            status.setRemarks(req.getRemark());
        }

        // Save & record history
        status = amlStatusRepository.save(status);
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, currentUser);
        amlHistoryRepository.save(history);

        // Convert to DTO
        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status);

        // Publish event for post-update side effects (Final Log, Telegram)
        eventPublisher.publishEvent(new AmlStatusChangedEvent(this, amlDto));

        return amlDto;
    }

    @Override
    public AllAmlResponseDto getAllAml(AllAmlRequestDto request) {
        log.info("Fetching all AML statuses with status: {} and search: {}", request.getStatus(), request.getSearch());
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        Page<AmlStatus> page = amlStatusRepository.findByStatusAndSearch(request.getStatus(), request.getSearch(), pageable);

        List<AmlStatusDto> content = page.stream()
                .map(amlStatusMapper::toStatusDto)
                .collect(Collectors.toList());

        log.info("Found {} AML statuses", page.getTotalElements());
        return amlStatusMapper.mapToListDto(content, page);
    }

    @Override
    public AmlStatusDto getAmlById(Long id) {
        log.debug("Fetching AML status by id: {}", id);
        AmlStatus aml = amlStatusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aml not found with Id: " + id));
        return amlStatusMapper.toStatusDto(aml);
    }

    @Override
    public AmlHistoryDto getAmlHistoryById(Long id) {
        log.debug("Fetching AML history by id: {}", id);
        AmlHistory aml = amlHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aml history not found with Id: " + id));
        return amlHistoryMapper.toDto(aml);
    }

    @Override
    public AllAmlHistoryResponseDto getAllAmlHistory(AllAmlHistoryRequestDto request) {
        log.info("Fetching AML history with status: {}, search: {}, startDate: {}, endDate: {}",
                request.getStatus(), request.getSearch(), request.getStartDate(), request.getEndDate());

        LocalDateTime startDateTime = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null;
        String search = request.getSearch() != null ? request.getSearch().toLowerCase() : null;

        Specification<AmlHistory> spec = Specification.where(null);

        if (startDateTime != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
        }
        if (endDateTime != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
        }
        if (request.getStatus() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), request.getStatus()));
        }
        if (search != null && !search.isEmpty()) {
            String pattern = "%" + search + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("familyName")), pattern),
                    cb.like(cb.lower(root.get("givenName")), pattern),
                    cb.like(cb.lower(root.get("lastNameKh")), pattern),
                    cb.like(cb.lower(root.get("firstNameKh")), pattern),
                    cb.like(cb.lower(root.get("phoneNumber")), pattern),
                    cb.like(cb.lower(root.get("legalId")), pattern)
            ));
        }

        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AmlHistory> page = amlHistoryRepository.findAll(spec, pageable);

        List<AmlHistoryDto> content = page.stream()
                .map(amlHistoryMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} AML history records", page.getTotalElements());
        return amlHistoryMapper.mapToListDto(content, page);
    }

    @Override
    @Transactional
    public void updateExternalAmlStatus(ExternalAmlStatusUpdateDto request) {
        log.info("Updating external AML status for customerId: {}", request.getCustomerId());
        String legalId = request.getCustomerId();
        if (legalId != null && legalId.toUpperCase().startsWith("OAO")) {
            legalId = legalId.substring(3); // Remove "OAO" prefix
        }
        Optional<AmlStatus> amlStatusOpt = amlStatusRepository.findByLegalId(legalId);
        if (amlStatusOpt.isPresent()) {
            AmlStatus amlStatus = amlStatusOpt.get();
            amlStatus.setAmlExternalRiskLevel(AppConstants.RISK_LOW);
            amlStatus.setStatus(AmlStatusEnum.APPROVE);
            if (request.getUpdateFrom() != null) {
                amlStatus.setAmlExternalServiceName(request.getUpdateFrom());
            }
            amlStatusRepository.save(amlStatus);
            log.info("Successfully updated AML Status Risk Level to Low for Legal ID: {}", legalId);
        } else {
            log.warn("Attempted to update AML Status for non-existent Legal ID: {}", legalId);
            throw new NotFoundException("AML Status not found for Legal ID: " + legalId);
        }
    }

    private void populateAddressFields(AmlStatus status, CreateAmlRequestDto requestDto) {
        // ------------------ Current Address ------------------
        if (requestDto.getCustomerCurrentProvince() != null && !requestDto.getCustomerCurrentProvince().isEmpty()) {
            LocationCodesDto currentAddress = masterDataServiceHelper.resolveCurrentAddress(requestDto);
            status.setCurrentAddressName(masterDataServiceHelper.buildFullAddressName(currentAddress));
            status.setCurrentAddressCode(masterDataServiceHelper.buildFullAddressCode(currentAddress));
        }

        // ------------------ Place of Birth ------------------
        if (requestDto.getCustomerPobProvince() != null && !requestDto.getCustomerPobProvince().isEmpty()) {
            LocationCodesDto pobAddress = masterDataServiceHelper.resolvePlaceOfBirth(requestDto);
            status.setPlaceOfBirthName(masterDataServiceHelper.buildFullAddressName(pobAddress));
            status.setPlaceOfBirthCode(masterDataServiceHelper.buildFullAddressCode(pobAddress));
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
