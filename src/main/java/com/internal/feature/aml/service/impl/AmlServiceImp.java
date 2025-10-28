package com.internal.feature.aml.service.impl;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.AllAmlHistoryRequestDto;
import com.internal.feature.aml.dto.request.AllAmlRequestDto;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
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
import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.telegram_alerts.service.serviceImpl.OpenAccountTelegramAlertServiceImpl;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final UserMapper userMapper;
    private final OpenAccountTelegramAlertServiceImpl alertTelegramService;

    @Override
    @Transactional
    public AmlStatusDto createAmlStatus(CreateAmlRequestDto requestDto) {

        AmlStatus status = amlStatusMapper.fromCreateDto(requestDto, userMapper);
        status.setRejectedBy(null);
        status = amlStatusRepository.save(status);

        // Create history for PENDING
        AmlHistory history = amlHistoryMapper.createHistoryFromStatusChange(
                status.getOriginalRequest(),
                status.getOriginalResponse(),
                null,
                status.getStatus(),
                null
        );
        amlHistoryRepository.save(history);

        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status, userMapper);

        // Send Telegram notification for PENDING
        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send PENDING AML Telegram notification: {}", e.getMessage());
        }

        return amlDto;
    }

    @Transactional
    @Override
    public AmlStatusDto approveAmlStatus(Long id) {
        UserEntity currentUser = securityUtils.getCurrentUser();

        AmlStatus status = amlStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AML Status not found"));

        AmlStatusEnum oldStatus = status.getStatus();

        status.setStatus(AmlStatusEnum.APPROVE);
        status.setApprovedBy(currentUser);
        status.setRejectedBy(null);
        status = amlStatusRepository.save(status);

        // Create history
        AmlHistory history = amlHistoryMapper.createHistoryFromStatus(status, oldStatus, currentUser);
        amlHistoryRepository.save(history);

        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status, userMapper);

        // Send Telegram notification
        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send AML APPROVE Telegram notification: {}", e.getMessage());
        }

        return amlDto;
    }

    @Transactional
    @Override
    public AmlStatusDto rejectAmlStatus(Long id) {
        UserEntity currentUser = securityUtils.getCurrentUser();

        AmlStatus status = amlStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AML Status not found"));

        AmlStatusEnum oldStatus = status.getStatus();

        status.setStatus(AmlStatusEnum.REJECT);
        status.setRejectedBy(currentUser);
        status.setApprovedBy(null);
        status = amlStatusRepository.save(status);

        // Create history
        AmlHistory history = amlHistoryMapper.createHistoryFromStatus(status, oldStatus, currentUser);
        amlHistoryRepository.save(history);

        AmlStatusDto amlDto = amlStatusMapper.toStatusDto(status, userMapper);

        // Send Telegram notification
        try {
            alertTelegramService.sendTelegramAmlProcess(amlDto);
        } catch (Exception e) {
            log.error("Failed to send AML REJECT Telegram notification: {}", e.getMessage());
        }

        return amlDto;
    }

    @Override
    public AllAmlResponseDto getAllAml(AllAmlRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        var spec = AmlStatusSpecification.hasStatus(request.getStatus())
                .and(AmlStatusSpecification.search(request.getSearch()));

        Page<AmlStatus> page = amlStatusRepository.findAll(spec, pageable);

        List<AmlStatusDto> content = page.stream()
                .map(aml -> amlStatusMapper.toStatusDto(aml, userMapper))
                .collect(Collectors.toList());

        return amlStatusMapper.mapToListDto(content, page);
    }

    @Override
    public AllAmlHistoryResponseDto getAllAmlHistory(AllAmlHistoryRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        var spec = AmlHistorySpecification.createdBetween(request.getStartDate(), request.getEndDate())
                .and(AmlHistorySpecification.search(request.getSearch()));

        Page<AmlHistory> page = amlHistoryRepository.findAll(spec, pageable);

        List<AmlHistoryDto> content = page.stream()
                .map(history -> amlHistoryMapper.toDto(history, userMapper))
                .collect(Collectors.toList());

        return amlHistoryMapper.mapToListDto(content, page);
    }
}
