package com.internal.feature.logs_report.repository;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportProjection;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AccountOnlineReportLogRepository extends JpaRepository<AccountOnlineReportLog, UUID> {

    @Query("SELECT " +
            "CAST(a.createdAt AS date) as date, " +
            "a.status as status, " +
            "COUNT(a) as count " +
            "FROM AccountOnlineReportLog a " +
            "WHERE a.createdAt >= :fromDate AND a.createdAt < :toDate " +
            "GROUP BY CAST(a.createdAt AS date), a.status " +
            "ORDER BY CAST(a.createdAt AS date)")
    List<AccountOnlineReportProjection> getReportByDateRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("SELECT a FROM AccountOnlineReportLog a WHERE " +
           "(:fromDate IS NULL OR a.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR a.createdAt <= :toDate) AND " +
           "(:statuses IS NULL OR a.status IN :statuses) " +
           "ORDER BY a.createdAt DESC")
    List<AccountOnlineReportLog> findByDateRangeAndStatuses(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("statuses") List<OpenAccStatusEnum> statuses);

    @Query("SELECT a FROM AccountOnlineReportLog a WHERE " +
           "(:fromDate IS NULL OR a.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR a.createdAt <= :toDate) AND " +
           "(:statuses IS NULL OR a.status IN :statuses) " +
           "ORDER BY a.createdAt DESC")
    Page<AccountOnlineReportLog> findByDateRangeAndStatusesPaged(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("statuses") List<OpenAccStatusEnum> statuses,
            Pageable pageable);
}
