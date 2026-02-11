package com.internal.feature.aml.repository;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.model.AmlStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AmlStatusRepository extends JpaRepository<AmlStatus, Long> {

    Optional<AmlStatus> findByLegalId(String legalId);

    @Query("SELECT a FROM AmlStatus a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.familyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.givenName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.lastNameKh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.firstNameKh) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.legalId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.createdAt DESC")
    Page<AmlStatus> findByStatusAndSearch(@Param("status") AmlStatusEnum status, @Param("search") String search, Pageable pageable);

    long countByStatus(AmlStatusEnum amlStatusEnum);
}
