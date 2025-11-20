package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.dto.response.AccountOnlineReportProjection;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AccountOnlineReportLogRepository extends JpaRepository<AccountOnlineReportLog, UUID>,
        JpaSpecificationExecutor<AccountOnlineReportLog> {
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
}
