package com.internal.feature.attendance.repository;

import com.internal.enumation.AttendanceStatus;
import com.internal.enumation.LeaveRequest;
import com.internal.feature.attendance.models.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long>,
        JpaSpecificationExecutor<AttendanceEntity> {

    /**
     * Find duplicate leave requests on the same day(s) with the same leave type.
     * Only checks for same leaveRequest type to allow different types on same day
     * (e.g., MORNING and AFTERNOON on same day is allowed)
     */
    @Query("SELECT a FROM AttendanceEntity a " +
            "WHERE a.user.id = :userId " +
            "AND a.leaveRequest = :leaveRequest " +
            "AND a.status NOT IN ('REJECTED', 'CANCELLED') " +
            "AND ((a.startDate <= :endDate AND a.endDate >= :startDate))")
    List<AttendanceEntity> findDuplicateLeaveRequests(
            @Param("userId") Long userId,
            @Param("leaveRequest") LeaveRequest leaveRequest,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}