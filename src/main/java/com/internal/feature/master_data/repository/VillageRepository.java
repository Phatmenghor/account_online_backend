package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long>, JpaSpecificationExecutor<Village> {
    List<Village> findByCommuneCommuneCode(String communeCode);
    Optional<Village> findByVillageCode(String villageCode);
    boolean existsByVillageCode(String villageCode);
    Optional<Village> findFirstByCommuneCommuneCodeAndVillageKh(String communeCode, String villageKh);
}
