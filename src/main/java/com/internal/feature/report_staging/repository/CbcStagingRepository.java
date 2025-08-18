
package com.internal.feature.report_staging.repository;

import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CbcStagingRepository extends JpaRepository<CbcStagingRecordEntity, Long>, JpaSpecificationExecutor<CbcStagingRecordEntity> {
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CbcStagingRecordEntity")
    void deleteAllStaging();
    
    @Query("SELECT COUNT(s) FROM CbcStagingRecordEntity s")
    long countAllStaging();
}