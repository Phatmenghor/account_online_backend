package com.internal.feature.attendance.repository;

import com.internal.enumation.AttendanceStatus;
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

    @Query("SELECT a FROM AttendanceEntity a " +
           "WHERE a.user.id = :userId " +
           "AND a.status NOT IN ('REJECTED', 'CANCELLED') " +
           "AND ((a.startDate <= :endDate AND a.endDate >= :startDate))")
    List<AttendanceEntity> findOverlappingAttendances(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    long countByUserIdAndStatus(Long userId, AttendanceStatus status);
}