package com.internal.feature.report_staging.repository;

import com.internal.feature.report_staging.models.CbcSecurityInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CbcSecurityInformationRepository extends JpaRepository<CbcSecurityInformationEntity, Long> {
}
