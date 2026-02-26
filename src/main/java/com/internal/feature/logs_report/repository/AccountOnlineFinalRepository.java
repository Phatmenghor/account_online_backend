package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.AccountOnlineFinal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountOnlineFinalRepository extends JpaRepository<AccountOnlineFinal, UUID> {

        Optional<AccountOnlineFinal> findByLegalId(String legalId);

        Optional<AccountOnlineFinal> findTopByCifOrLegalIdOrderByCreatedAtDesc(String cif, String legalId);

        @Query("SELECT a.legalGender, COUNT(a) FROM AccountOnlineFinal a " +
                        "WHERE a.createdAt >= :startDate AND a.createdAt < :endDate " +
                        "GROUP BY a.legalGender")
        List<Object[]> countByGenderAndDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a FROM AccountOnlineFinal a WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(a.cif) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(a.legalId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "ORDER BY a.createdAt DESC")
        Page<AccountOnlineFinal> findBySearch(@Param("search") String search, Pageable pageable);

        // Excel
        @Query("SELECT a FROM AccountOnlineFinal a WHERE " +
                "(:search IS NULL OR :search = '' OR " +
                "LOWER(a.cif) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                "LOWER(a.legalId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                "AND (CAST(:fromDate AS java.time.LocalDateTime) IS NULL OR a.createdAt >= :fromDate) " +
                "AND (CAST(:toDate AS java.time.LocalDateTime) IS NULL OR a.createdAt < :toDate) " +
                "ORDER BY a.createdAt DESC")
        List<AccountOnlineFinal> findBySearchExcel(
                @Param("search") String search,
                @Param("fromDate") LocalDateTime fromDate,
                @Param("toDate") LocalDateTime toDate
        );
}
