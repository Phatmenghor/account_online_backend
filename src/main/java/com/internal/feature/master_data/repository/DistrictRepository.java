package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.District;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByProvinceProvinceCode(String provinceCode);
    Optional<District> findByDistrictCode(String districtCode);
    boolean existsByDistrictCode(String districtCode);
    Optional<District> findFirstByProvinceProvinceCodeAndDistrictKh(String provinceCode, String districtKh);

    @Query("SELECT d FROM District d WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(d.districtEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.districtKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.districtCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<District> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT d FROM District d WHERE d.province.provinceCode = :provinceCode AND " +
           "(:search IS NULL OR :search = '' OR LOWER(d.districtEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.districtKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.districtCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<District> findByProvinceCodeAndSearch(@Param("provinceCode") String provinceCode, @Param("search") String search, Pageable pageable);
}
