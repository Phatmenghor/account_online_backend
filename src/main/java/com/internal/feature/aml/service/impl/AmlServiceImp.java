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

    @Override
    public Optional<AmlStatus> findByLegalId(String legalId) {
        return amlStatusRepository.findByLegalId(legalId);
    }

    // -------------------------------
    // CREATE AML STATUS
    // -------------------------------
    @Override
    @Transactional
    public AmlStatusDto createAmlStatus(CreateAmlRequestDto requestDto) throws JsonProcessingException {
        AmlStatus status = amlStatusMapper.fromCreateDto(requestDto);
        status.setRejectedBy(null);
        status = amlStatusRepository.save(status);

        // Create initial history (PENDING)
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, null);
        amlHistoryRepository.save(history);

        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status);

        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send PENDING AML Telegram notification: {}", e.getMessage());
        }
        return amlDto;
    }

    // -------------------------------
    // UPDATE AML STATUS (APPROVE / REJECT / FUTURE)
    // -------------------------------
    @Override
    @Transactional
    public AmlStatusDto updateAmlStatus(Long id, UpdateAmlStatusDto req) throws JsonProcessingException {
        UserEntity currentUser = securityUtils.getCurrentUser();

        AmlStatus status = amlStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AML Status not found"));

        // Update status based on enum
        status.setStatus(req.getStatus());

        if (req.getStatus() == AmlStatusEnum.APPROVE) {
            status.setApprovedBy(currentUser);
            status.setRejectedBy(null);
        } else if (req.getStatus() == AmlStatusEnum.REJECT) {
            status.setRejectedBy(currentUser);
            status.setApprovedBy(null);
        } else {
            // For any other statuses, clear both fields
            status.setApprovedBy(null);
            status.setRejectedBy(null);
        }

        status = amlStatusRepository.save(status);

        // Create history
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(status, currentUser);
        amlHistoryRepository.save(history);

        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status);

        // Send Telegram notification
        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send AML {} Telegram notification: {}", req.getStatus(), e.getMessage());
        }

        return amlDto;
    }

    // -------------------------------
    // GET ALL AML
    // -------------------------------
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

    // -------------------------------
    // GET ALL AML HISTORY
    // -------------------------------
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
}
