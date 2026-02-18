package com.internal.feature.master_data.repository;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.Occupation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long> {
       boolean existsByNameKh(String nameKh);

       boolean existsByNameEn(String nameEn);

       Optional<Occupation> findFirstByOccupationCode(String occupationCode);

       @Query("SELECT o FROM Occupation o WHERE (:status IS NULL OR o.status = :status) " +
                     "AND (:search IS NULL OR :search = '' OR LOWER(o.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
                     "OR LOWER(o.nameKh) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                     "ORDER BY o.createdAt DESC")
       Page<Occupation> findByStatusAndSearch(@Param("status") StatusData status, @Param("search") String search,
                     Pageable pageable);

       @Query("SELECT o FROM Occupation o WHERE o.status = :status " +
                     "AND (:search IS NULL OR :search = '' OR LOWER(o.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
                     "OR LOWER(o.nameKh) LIKE LOWER(CONCAT('%', :search, '%')))")
       List<Occupation> findActiveBySearch(@Param("status") StatusData status, @Param("search") String search);
}
