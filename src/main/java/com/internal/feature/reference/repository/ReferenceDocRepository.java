package com.internal.feature.reference.repository;

import com.internal.feature.reference.models.ReferenceDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceDocRepository extends JpaRepository<ReferenceDoc, Long> , JpaSpecificationExecutor<ReferenceDoc> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);
}
