package com.internal.feature.reference.repository;

import com.internal.feature.reference.models.ReferenceBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceBankRepository extends JpaRepository<ReferenceBank, Long> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);
}
