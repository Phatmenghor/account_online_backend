package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long>, JpaSpecificationExecutor<Province> {
    Optional<Province> findByProvinceCode(String provinceCode);
    boolean existsByProvinceCode(String provinceCode);
}
