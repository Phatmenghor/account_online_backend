package com.internal.feature.report_staging.repository;

import com.internal.feature.report_staging.models.CbcMainRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CbcMainRecordRepository extends JpaRepository<CbcMainRecordEntity, Long>, JpaSpecificationExecutor<CbcMainRecordEntity> {
    
    @Modifying
    @Query("DELETE FROM CbcMainRecordEntity")
    void deleteAllData();
    
    Page<CbcMainRecordEntity> findByRequestStartDateBetweenOrRequestEndDateBetween(
            LocalDate startDate1, LocalDate endDate1,
            LocalDate startDate2, LocalDate endDate2,
            Pageable pageable);
    
    List<CbcMainRecordEntity> findByRequestStartDateBetweenOrRequestEndDateBetween(
            LocalDate startDate1, LocalDate endDate1,
            LocalDate startDate2, LocalDate endDate2);
    
    @Query("SELECT COUNT(m) FROM CbcMainRecordEntity m")
    long countAllRecords();
}