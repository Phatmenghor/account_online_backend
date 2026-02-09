package com.internal.feature.master_data.repository;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.LegalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegalTypeRepository extends JpaRepository<LegalType, Long> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);

    @Query("SELECT l FROM LegalType l WHERE (:status IS NULL OR l.status = :status) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(l.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.nameKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.legalTypeValue) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY l.createdAt DESC")
    Page<LegalType> findByStatusAndSearch(@Param("status") StatusData status, @Param("search") String search, Pageable pageable);

    @Query("SELECT l FROM LegalType l WHERE l.status = :status " +
           "AND (:search IS NULL OR :search = '' OR LOWER(l.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.nameKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.legalTypeValue) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<LegalType> findActiveBySearch(@Param("status") StatusData status, @Param("search") String search);
}
