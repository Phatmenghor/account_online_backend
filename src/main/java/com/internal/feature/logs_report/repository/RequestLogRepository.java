package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, UUID> {

    /**
     * Find log by UUID
     */
    Optional<RequestLog> findById(UUID id);

    /**
     * Find logs by username
     */
    List<RequestLog> findByUsernameOrderByCreatedAtDesc(String username);

    /**
     * Find logs by IP address
     */
    List<RequestLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    /**
     * Find recent logs (last 100 records)
     */
    List<RequestLog> findTop100ByOrderByCreatedAtDesc();

    /**
     * Find logs within date range
     */
    @Query("SELECT rl FROM RequestLog rl WHERE rl.createdAt BETWEEN :startDate AND :endDate ORDER BY rl.createdAt DESC")
    List<RequestLog> findLogsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find failed requests
     */
    @Query("SELECT rl FROM RequestLog rl WHERE rl.isSuccess = false ORDER BY rl.createdAt DESC")
    List<RequestLog> findFailedRequests();

    /**
     * Count requests by IP in time window
     */
    @Query("SELECT COUNT(rl) FROM RequestLog rl WHERE rl.ipAddress = :ipAddress AND rl.createdAt >= :since")
    Long countRequestsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Delete old logs (for maintenance)
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM acc_online_audit WHERE created_at < :cutoffDate AND api_key IS NULL", nativeQuery = true)
    int deleteLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find logs by username and date range
     */
    @Query("SELECT rl FROM RequestLog rl WHERE rl.username = :username " +
            "AND rl.createdAt BETWEEN :startDate AND :endDate ORDER BY rl.createdAt DESC")
    List<RequestLog> findByUsernameAndDateRange(@Param("username") String username,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
