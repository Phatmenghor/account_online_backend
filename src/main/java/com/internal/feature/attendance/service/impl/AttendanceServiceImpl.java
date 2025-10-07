package com.internal.feature.attendance.service.impl;

import com.internal.enumation.AttendanceStatus;
import com.internal.enumation.RoleEnum;
import com.internal.exceptions.error.BadRequestException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.exceptions.error.UnauthorizedException;
import com.internal.feature.attendance.dto.request.ApprovalRequestDto;
import com.internal.feature.attendance.dto.request.AttendanceRequestDto;
import com.internal.feature.attendance.dto.request.AttendanceUpdateRequestDto;
import com.internal.feature.attendance.dto.request.GetAllAttendanceRequestDto;
import com.internal.feature.attendance.dto.resposne.AllAttendanceResponseDto;
import com.internal.feature.attendance.dto.resposne.AttendanceResponseDto;
import com.internal.feature.attendance.mapper.AttendanceMapper;
import com.internal.feature.attendance.models.AttendanceEntity;
import com.internal.feature.attendance.repository.AttendanceRepository;
import com.internal.feature.attendance.service.AttendanceService;
import com.internal.feature.attendance.service.TelegramNotificationService;
import com.internal.feature.attendance.specification.AttendanceSpecification;
import com.internal.feature.auth.models.UserEntity;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final SecurityUtils securityUtils;
    private final TelegramNotificationService telegramNotificationService;

    @Override
    @Transactional
    public AttendanceResponseDto createAttendanceRequest(AttendanceRequestDto requestDto) {
        log.info("Creating attendance request for type: {}", requestDto.getType());

        UserEntity currentUser = securityUtils.getCurrentUser();

        validateDates(requestDto.getStartDate(), requestDto.getEndDate());
        checkOverlappingAttendances(currentUser.getId(), requestDto.getStartDate(), requestDto.getEndDate(), null);

        int totalDays = calculateTotalDays(requestDto.getStartDate(), requestDto.getEndDate());

        AttendanceEntity entity = attendanceMapper.toEntity(requestDto);
        entity.setUser(currentUser);
        entity.setTotalDays(totalDays);

        AttendanceEntity saved = attendanceRepository.save(entity);
        log.info("Attendance request created successfully with ID: {}", saved.getId());

        // Send Telegram notification
        try {
            telegramNotificationService.sendAttendanceRequestNotification(saved);
        } catch (Exception e) {
            log.error("Failed to send Telegram notification, but attendance was created: {}", e.getMessage());
        }

        return attendanceMapper.toDto(saved);
    }

    @Override
    public AllAttendanceResponseDto getAllAttendances(GetAllAttendanceRequestDto requestDto) {
        log.info("Fetching all attendances - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());

        Pageable pageable = createPageable(requestDto);
        Specification<AttendanceEntity> spec = buildSpecification(requestDto);
        Page<AttendanceEntity> page = attendanceRepository.findAll(spec, pageable);

        log.info("Retrieved {} attendances out of {} total", page.getContent().size(), page.getTotalElements());
        return attendanceMapper.mapToAllAttendanceResponseDto(page);
    }

    @Override
    public AllAttendanceResponseDto getMyAttendances(GetAllAttendanceRequestDto requestDto) {
        log.info("Fetching my attendances - page: {}, size: {}", requestDto.getPageNo(), requestDto.getPageSize());

        UserEntity currentUser = securityUtils.getCurrentUser();
        requestDto.setUserId(currentUser.getId());

        Pageable pageable = createPageable(requestDto);
        Specification<AttendanceEntity> spec = buildSpecification(requestDto);
        Page<AttendanceEntity> page = attendanceRepository.findAll(spec, pageable);

        log.info("Retrieved {} of my attendances", page.getContent().size());
        return attendanceMapper.mapToAllAttendanceResponseDto(page);
    }

    @Override
    public AttendanceResponseDto getAttendanceById(Long id) {
        log.info("Fetching attendance by ID: {}", id);

        AttendanceEntity entity = findAttendanceById(id);
        UserEntity currentUser = securityUtils.getCurrentUser();

        validateViewPermission(entity, currentUser);

        return attendanceMapper.toDto(entity);
    }

    @Override
    @Transactional
    public AttendanceResponseDto updateAttendance(Long id, AttendanceUpdateRequestDto requestDto) {
        log.info("Updating attendance with ID: {}", id);

        AttendanceEntity entity = findAttendanceById(id);
        UserEntity currentUser = securityUtils.getCurrentUser();

        validateOwnership(entity, currentUser);
        validatePendingStatus(entity);

        if (requestDto.getStartDate() != null && requestDto.getEndDate() != null) {
            validateDates(requestDto.getStartDate(), requestDto.getEndDate());
            checkOverlappingAttendances(currentUser.getId(), requestDto.getStartDate(),
                    requestDto.getEndDate(), id);

            int totalDays = calculateTotalDays(requestDto.getStartDate(), requestDto.getEndDate());
            entity.setTotalDays(totalDays);
        }

        attendanceMapper.updateEntityFromDto(requestDto, entity);

        AttendanceEntity updated = attendanceRepository.save(entity);
        log.info("Attendance updated successfully: {}", id);

        return attendanceMapper.toDto(updated);
    }

    @Override
    @Transactional
    public AttendanceResponseDto cancelAttendance(Long id) {
        log.info("Cancelling attendance with ID: {}", id);

        AttendanceEntity entity = findAttendanceById(id);
        UserEntity currentUser = securityUtils.getCurrentUser();

        validateOwnership(entity, currentUser);

        if (entity.getStatus() == AttendanceStatus.CANCELLED) {
            throw new BadRequestException("Attendance request is already cancelled");
        }

        entity.setStatus(AttendanceStatus.CANCELLED);

        AttendanceEntity cancelled = attendanceRepository.save(entity);
        log.info("Attendance cancelled successfully: {}", id);

        return attendanceMapper.toDto(cancelled);
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        log.info("Deleting attendance with ID: {}", id);

        AttendanceEntity entity = findAttendanceById(id);
        UserEntity currentUser = securityUtils.getCurrentUser();

        validateOwnership(entity, currentUser);
        validatePendingStatus(entity);

        attendanceRepository.delete(entity);
        log.info("Attendance deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public AttendanceResponseDto approveOrRejectAttendance(Long id, ApprovalRequestDto approvalDto) {
        log.info("Processing approval for attendance ID: {} with status: {}", id, approvalDto.getStatus());

        AttendanceEntity entity = findAttendanceById(id);
        UserEntity currentUser = securityUtils.getCurrentUser();

        validatePendingStatus(entity);
        validateApprovalStatus(approvalDto.getStatus());

        entity.setStatus(approvalDto.getStatus());
        entity.setApprovedBy(currentUser);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApprovalNotes(approvalDto.getApprovalNotes());

        AttendanceEntity processed = attendanceRepository.save(entity);
        log.info("Attendance {} successfully by {}: {}",
                approvalDto.getStatus(), currentUser.getUsername(), id);

        // Send Telegram notification
        try {
            if (approvalDto.getStatus() == AttendanceStatus.APPROVED) {
                telegramNotificationService.sendAttendanceApprovalNotification(processed);
            } else if (approvalDto.getStatus() == AttendanceStatus.REJECTED) {
                telegramNotificationService.sendAttendanceRejectionNotification(processed);
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram notification, but approval was processed: {}", e.getMessage());
        }

        return attendanceMapper.toDto(processed);
    }

    // ============================================
    // PRIVATE HELPER METHODS - Validation
    // ============================================

    private AttendanceEntity findAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Attendance not found with ID: {}", id);
                    return new NotFoundException("Attendance not found with ID: " + id);
                });
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (endDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("End date cannot be in the past");
        }
    }

    private void checkOverlappingAttendances(Long userId, LocalDate startDate, LocalDate endDate, Long excludeId) {
        List<AttendanceEntity> overlapping = attendanceRepository.findOverlappingAttendances(
                userId, startDate, endDate);

        if (excludeId != null) {
            overlapping.removeIf(a -> a.getId().equals(excludeId));
        }

        if (!overlapping.isEmpty()) {
            log.warn("Overlapping attendance found for user: {}", userId);
            throw new BadRequestException("You already have an attendance request for these dates");
        }
    }

    private void validateOwnership(AttendanceEntity entity, UserEntity currentUser) {
        if (!entity.getUser().getId().equals(currentUser.getId())) {
            log.warn("Unauthorized action attempt by user: {}", currentUser.getUsername());
            throw new UnauthorizedException("You can only manage your own attendance requests");
        }
    }

    private void validateViewPermission(AttendanceEntity entity, UserEntity currentUser) {
        boolean isOwner = entity.getUser().getId().equals(currentUser.getId());
        boolean isSuper = hasRole(currentUser, RoleEnum.SUPER);

        if (!isOwner && !isSuper) {
            log.warn("Unauthorized view attempt by user: {}", currentUser.getUsername());
            throw new UnauthorizedException("You can only view your own attendance requests");
        }
    }

    private void validatePendingStatus(AttendanceEntity entity) {
        if (entity.getStatus() != AttendanceStatus.PENDING) {
            throw new BadRequestException("Can only modify PENDING attendance requests");
        }
    }

    private void validateApprovalStatus(AttendanceStatus status) {
        if (status != AttendanceStatus.APPROVED && status != AttendanceStatus.REJECTED) {
            throw new BadRequestException("Status must be either APPROVED or REJECTED");
        }
    }

    // ============================================
    // PRIVATE HELPER METHODS - Utilities
    // ============================================

    private boolean hasRole(UserEntity user, RoleEnum role) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName() == role);
    }

    private int calculateTotalDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private Pageable createPageable(GetAllAttendanceRequestDto requestDto) {
        return PageRequest.of(
                Math.max(requestDto.getPageNo() - 1, 0),
                Math.max(requestDto.getPageSize(), 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private Specification<AttendanceEntity> buildSpecification(GetAllAttendanceRequestDto requestDto) {
        return AttendanceSpecification.withFilters(
                requestDto.getUserId(),
                requestDto.getStatus(),
                requestDto.getType(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                requestDto.getSearch()
        );
    }
}