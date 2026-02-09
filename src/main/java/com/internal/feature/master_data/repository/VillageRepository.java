package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Village;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long> {
    List<Village> findByCommuneCommuneCode(String communeCode);
    Optional<Village> findByVillageCode(String villageCode);
    boolean existsByVillageCode(String villageCode);
    Optional<Village> findFirstByCommuneCommuneCodeAndVillageKh(String communeCode, String villageKh);

    @Query("SELECT v FROM Village v WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(v.villageEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.villageKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.villageCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Village> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT v FROM Village v WHERE v.commune.communeCode = :communeCode AND " +
           "(:search IS NULL OR :search = '' OR LOWER(v.villageEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.villageKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.villageCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Village> findByCommuneCodeAndSearch(@Param("communeCode") String communeCode, @Param("search") String search, Pageable pageable);
}
