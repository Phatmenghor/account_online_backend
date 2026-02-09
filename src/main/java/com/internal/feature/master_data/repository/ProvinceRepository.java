package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Province;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Optional<Province> findByProvinceCode(String provinceCode);
    boolean existsByProvinceCode(String provinceCode);
    Optional<Province> findFirstByProvinceKh(String provinceKh);

    @Query("SELECT p FROM Province p WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(p.provinceEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.provinceKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.provinceCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Province> findBySearch(@Param("search") String search, Pageable pageable);
}
