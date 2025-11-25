package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, Long>, JpaSpecificationExecutor<Commune> {
    List<Commune> findByDistrictDistrictCode(String districtCode);
    Optional<Commune> findByCommuneCode(String communeCode);
    boolean existsByCommuneCode(String communeCode);
    Optional<Commune> findFirstByDistrictDistrictCodeAndCommuneKh(String districtCode, String communeKh);
}
