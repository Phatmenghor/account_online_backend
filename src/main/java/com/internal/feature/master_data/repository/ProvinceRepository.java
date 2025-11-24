package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, String>, JpaSpecificationExecutor<Province> {
}
