
package com.internal.feature.report_staging.repository;

import com.internal.feature.report_staging.models.CbcFinalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CbcFinalRepository extends JpaRepository<CbcFinalRecordEntity, Long>, JpaSpecificationExecutor<CbcFinalRecordEntity> {
    
    @Query("SELECT COUNT(f) FROM CbcFinalRecordEntity f")
    long countAllFinal();
    
    @Query("SELECT DISTINCT f.batchSessionId, f.batchSessionDate, f.processedDate, COUNT(f) as recordCount, f.createdBy " +
           "FROM CbcFinalRecordEntity f " +
           "GROUP BY f.batchSessionId, f.batchSessionDate, f.processedDate, f.createdBy " +
           "ORDER BY f.batchSessionDate DESC, f.processedDate DESC")
    List<Object[]> findAllBatchSessionsSummary();
    
    @Query("SELECT DISTINCT f.batchSessionId, f.batchSessionDate, f.processedDate, COUNT(f) as recordCount, f.createdBy " +
           "FROM CbcFinalRecordEntity f " +
           "WHERE f.batchSessionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY f.batchSessionId, f.batchSessionDate, f.processedDate, f.createdBy " +
           "ORDER BY f.batchSessionDate DESC, f.processedDate DESC")
    List<Object[]> findBatchSessionsSummaryByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT f.batchSessionId, f.batchSessionDate, f.processedDate, COUNT(f) as recordCount, f.createdBy " +
           "FROM CbcFinalRecordEntity f " +
           "WHERE LOWER(f.batchSessionId) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "GROUP BY f.batchSessionId, f.batchSessionDate, f.processedDate, f.createdBy " +
           "ORDER BY f.batchSessionDate DESC, f.processedDate DESC")
    List<Object[]> findBatchSessionsSummaryBySearch(@Param("search") String search);
}