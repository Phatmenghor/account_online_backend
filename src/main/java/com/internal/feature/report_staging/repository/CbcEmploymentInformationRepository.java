package com.internal.feature.report_staging.repository;

import com.internal.feature.report_staging.models.CbcEmploymentInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CbcEmploymentInformationRepository extends JpaRepository<CbcEmploymentInformationEntity, Long> {
}