package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Commune;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, Long> {
    List<Commune> findByDistrictDistrictCode(String districtCode);
    Optional<Commune> findByCommuneCode(String communeCode);
    boolean existsByCommuneCode(String communeCode);
    Optional<Commune> findFirstByDistrictDistrictCodeAndCommuneKh(String districtCode, String communeKh);

    @Query("SELECT c FROM Commune c WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(c.communeEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.communeKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.communeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Commune> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Commune c WHERE c.district.districtCode = :districtCode AND " +
           "(:search IS NULL OR :search = '' OR LOWER(c.communeEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.communeKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.communeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Commune> findByDistrictCodeAndSearch(@Param("districtCode") String districtCode, @Param("search") String search, Pageable pageable);
}
