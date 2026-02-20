package com.internal.feature.aml.repository;

import com.internal.feature.aml.model.AmlHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AmlHistoryRepository extends JpaRepository<AmlHistory, Long> {

    @Query(value = "SELECT * FROM acc_online_aml_history a WHERE " +
           "(:startDate IS NULL OR a.created_at >= :startDate) AND " +
           "(:endDate IS NULL OR a.created_at <= :endDate) AND " +
           "(CAST(:status AS VARCHAR) IS NULL OR a.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.family_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.given_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.last_name_kh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.first_name_kh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.phone_number) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.legal_id) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM acc_online_aml_history a WHERE " +
           "(:startDate IS NULL OR a.created_at >= :startDate) AND " +
           "(:endDate IS NULL OR a.created_at <= :endDate) AND " +
           "(CAST(:status AS VARCHAR) IS NULL OR a.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.family_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.given_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.last_name_kh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.first_name_kh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.phone_number) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.legal_id) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Page<AmlHistory> findByFilters(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("status") String status,
                                   @Param("search") String search,
                                   Pageable pageable);
}
