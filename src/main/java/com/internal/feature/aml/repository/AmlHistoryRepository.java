package com.internal.feature.aml.repository;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.model.AmlHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AmlHistoryRepository extends JpaRepository<AmlHistory, Long> {

    @Query("SELECT a FROM AmlHistory a WHERE " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.familyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.givenName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.lastNameKh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.firstNameKh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.legalId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.createdAt DESC")
    Page<AmlHistory> findByFilters(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") AmlStatusEnum status, @Param("search") String search, Pageable pageable);
}
