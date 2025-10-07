package com.internal.feature.attendance.service.impl;

import com.internal.enumation.AttendanceStatus;
import com.internal.enumation.LeaveRequest;
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
        log.info("Creating attendance request for type: {} with leaveRequest: {}",
                requestDto.getType(), requestDto.getLeaveRequest());

        UserEntity currentUser = securityUtils.getCurrentUser();

        validateDates(requestDto.getStartDate(), requestDto.getEndDate());

        double totalDays = calculateTotalDays(requestDto.getStartDate(),
                requestDto.getEndDate(), requestDto.getLeaveRequest());

        AttendanceEntity entity = attendanceMapper.toEntity(requestDto);
        entity.setUser(currentUser);
        entity.setTotalDays(totalDays);

        AttendanceEntity saved = attendanceRepository.save(entity);
        log.info("Attendance request created successfully with ID: {}, totalDays: {}",
                saved.getId(), totalDays);

        // Send Telegram notification
        try {
            telegramNotificationService.sendAttendanceRequestNotification(saved);
        } catch (Exception e) {
            log.error("Failed to send Telegram notification, but attendance was created: {}",
                    e.getMessage());
        }

        return attendanceMapper.toDto(saved);
    }

    @Override
    public AllAttendanceResponseDto getAllAttendances(GetAllAttendanceRequestDto requestDto) {
        log.info("Fetching all attendances - page: {}, size: {}",
                requestDto.getPageNo(), requestDto.getPageSize());

        Pageable pageable = createPageable(requestDto);
        Specification<AttendanceEntity> spec = buildSpecification(requestDto);
        Page<AttendanceEntity> page = attendanceRepository.findAll(spec, pageable);

        log.info("Retrieved {} attendances out of {} total",
                page.getContent().size(), page.getTotalElements());
        return attendanceMapper.mapToAllAttendanceResponseDto(page);
    }

    @Override
    public List<AttendanceResponseDto> getAllListAttendances(GetAllAttendanceRequestDto requestDto) {
        log.info("Fetching all attendances");

        UserEntity currentUser = securityUtils.getCurrentUser();

        Specification<AttendanceEntity> spec = buildSpecification(requestDto);
        List<AttendanceEntity> page = attendanceRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        log.info("Retrieved attendances out");
        return attendanceMapper.toDtoList(page);
    }

    @Override
    public AllAttendanceResponseDto getMyAttendances(GetAllAttendanceRequestDto requestDto) {
        log.info("Fetching my attendances - page: {}, size: {}",
                requestDto.getPageNo(), requestDto.getPageSize());

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

        // Determine which leaveRequest to use for calculation
        LeaveRequest leaveRequestForCalc = requestDto.getLeaveRequest() != null
                ? requestDto.getLeaveRequest()
                : entity.getLeaveRequest();

        // Determine which dates to use for calculation
        LocalDate startDateForCalc = requestDto.getStartDate() != null
                ? requestDto.getStartDate()
                : entity.getStartDate();
        LocalDate endDateForCalc = requestDto.getEndDate() != null
                ? requestDto.getEndDate()
                : entity.getEndDate();

        // Validate dates if any date is being changed
        if (requestDto.getStartDate() != null || requestDto.getEndDate() != null) {
            validateDates(startDateForCalc, endDateForCalc);
        }

        // Check for duplicates if dates or leaveRequest is changing
        if (requestDto.getStartDate() != null || requestDto.getEndDate() != null
                || requestDto.getLeaveRequest() != null) {

            double totalDays = calculateTotalDays(startDateForCalc, endDateForCalc, leaveRequestForCalc);
            entity.setTotalDays(totalDays);
            log.info("Recalculated totalDays: {}", totalDays);
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
        log.info("Processing approval for attendance ID: {} with status: {}",
                id, approvalDto.getStatus());

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
            log.error("Failed to send Telegram notification, but approval was processed: {}",
                    e.getMessage());
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
    }

//    /**
//     * Check for duplicate leave requests on the same day(s) with the same leave type.
//     * Users can request different leave types on the same day (e.g., MORNING and AFTERNOON).
//     */
//    private void checkDuplicateLeaveRequests(Long userId, LeaveRequest leaveRequest,
//                                             LocalDate startDate, LocalDate endDate, Long excludeId) {
//        List<AttendanceEntity> duplicates = attendanceRepository.findDuplicateLeaveRequests(
//                userId, leaveRequest, startDate, endDate);
//
//        if (excludeId != null) {
//            duplicates.removeIf(a -> a.getId().equals(excludeId));
//        }
//
//        if (!duplicates.isEmpty()) {
//            log.warn("Duplicate leave request found for user: {} with leaveRequest: {} on dates: {} to {}",
//                    userId, leaveRequest, startDate, endDate);
//            throw new BadRequestException(
//                    String.format("You already have a %s leave request for these dates",
//                            leaveRequest.name()));
//        }
//    }

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

    /**
     * Calculate total days based on date range and leave request type
     *
     * @param startDate Start date of leave
     * @param endDate End date of leave
     * @param leaveRequest Type of leave (FULL_DAY, MORNING, AFTERNOON)
     * @return Total days as double (0.5 for half days, 1.0 for full days)
     */
    private double calculateTotalDays(LocalDate startDate, LocalDate endDate, LeaveRequest leaveRequest) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Default to FULL_DAY if not specified
        if (leaveRequest == null) {
            leaveRequest = LeaveRequest.FULL_DAY;
        }

        switch (leaveRequest) {
            case MORNING:
            case AFTERNOON:
                // Half day leave = 0.5 days per day in range
                return daysBetween * 0.5;
            case FULL_DAY:
            default:
                // Full day leave = 1.0 days per day in range
                return (double) daysBetween;
        }
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