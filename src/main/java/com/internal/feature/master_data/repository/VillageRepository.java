package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface VillageRepository extends JpaRepository<Village, String>, JpaSpecificationExecutor<Village> {
    List<Village> findByCommuneCommuneCode(String communeCode);
}
