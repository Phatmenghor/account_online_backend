package com.internal.feature.reference.repository;

import com.internal.feature.reference.models.Occupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long>, JpaSpecificationExecutor<Occupation> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);
    Optional<Occupation> findByOccupationCode(String occupationCode);
}

