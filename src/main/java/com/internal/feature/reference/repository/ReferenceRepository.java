package com.internal.feature.reference.repository;

import com.internal.feature.reference.models.Reference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceRepository extends JpaRepository<Reference, Long>, JpaSpecificationExecutor<Reference> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);
}
