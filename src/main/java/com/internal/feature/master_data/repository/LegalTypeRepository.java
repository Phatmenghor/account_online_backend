package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.LegalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalTypeRepository extends JpaRepository<LegalType, Long> , JpaSpecificationExecutor<LegalType> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);
}
