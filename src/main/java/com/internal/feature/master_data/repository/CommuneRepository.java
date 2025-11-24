package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, String>, JpaSpecificationExecutor<Commune> {
    List<Commune> findByDistrictDistrictCode(String districtCode);
}
