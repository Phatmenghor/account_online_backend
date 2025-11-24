package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface DistrictRepository extends JpaRepository<District, String>, JpaSpecificationExecutor<District> {
    List<District> findByProvinceProvinceCode(String provinceCode);
}
