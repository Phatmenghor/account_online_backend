package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.MaritalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MaritalStatusRepository  extends JpaRepository<MaritalStatus, Long>, JpaSpecificationExecutor<MaritalStatus> {
    boolean existsByNameKh(String nameKh);
    boolean existsByNameEn(String nameEn);
}
