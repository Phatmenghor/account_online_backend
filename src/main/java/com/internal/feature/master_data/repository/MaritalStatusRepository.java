package com.internal.feature.master_data.repository;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.MaritalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaritalStatusRepository extends JpaRepository<MaritalStatus, Long> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);

    @Query("SELECT m FROM MaritalStatus m WHERE (:status IS NULL OR m.status = :status) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(m.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.nameKh) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY m.createdAt DESC")
    Page<MaritalStatus> findByStatusAndSearch(@Param("status") StatusData status, @Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM MaritalStatus m WHERE m.status = :status " +
           "AND (:search IS NULL OR :search = '' OR LOWER(m.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.nameKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<MaritalStatus> findActiveBySearch(@Param("status") StatusData status, @Param("search") String search);
}
