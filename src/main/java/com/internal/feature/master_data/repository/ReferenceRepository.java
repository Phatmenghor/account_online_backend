package com.internal.feature.master_data.repository;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.Reference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferenceRepository extends JpaRepository<Reference, Long> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);

    @Query("SELECT r FROM Reference r WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(r.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.nameKh) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.createdAt DESC")
    Page<Reference> findByStatusAndSearch(@Param("status") StatusData status, @Param("search") String search, Pageable pageable);

    @Query("SELECT r FROM Reference r WHERE r.status = :status " +
           "AND (:search IS NULL OR :search = '' OR LOWER(r.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.nameKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Reference> findActiveBySearch(@Param("status") StatusData status, @Param("search") String search);
}
