package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long>, JpaSpecificationExecutor<District> {
    List<District> findByProvinceProvinceCode(String provinceCode);
    Optional<District> findByDistrictCode(String districtCode);
    boolean existsByDistrictCode(String districtCode);
    Optional<District> findFirstByProvinceProvinceCodeAndDistrictKh(String provinceCode, String districtKh);
}
