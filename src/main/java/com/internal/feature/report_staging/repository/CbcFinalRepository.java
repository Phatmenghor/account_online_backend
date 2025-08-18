package com.internal.feature.report_staging.repository;

import com.internal.feature.report_staging.models.CbcFinalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CbcFinalRepository extends JpaRepository<CbcFinalRecordEntity, UUID>,
        JpaSpecificationExecutor<CbcFinalRecordEntity> {
    
    @Query("SELECT COUNT(f) FROM CbcFinalRecordEntity f")
    long countAllFinal();
    
    @Query("SELECT COUNT(f) FROM CbcFinalRecordEntity f WHERE f.archiveStatus = 'ACTIVE'")
    long countActiveFinal();
    
    @Query("SELECT COUNT(f) FROM CbcFinalRecordEntity f WHERE f.batchSessionId = :batchSessionId")
    long countByBatchSession(@Param("batchSessionId") String batchSessionId);
    
    @Query("SELECT COUNT(f) FROM CbcFinalRecordEntity f WHERE f.wasUpdated = true")
    long countUpdatedRecords();
    
    @Query("SELECT DISTINCT f.batchSessionId, f.batchSessionDate, f.processedDate, COUNT(f) as recordCount, f.createdBy " +
           "FROM CbcFinalRecordEntity f " +
           "WHERE f.archiveStatus = 'ACTIVE' " +
           "GROUP BY f.batchSessionId, f.batchSessionDate, f.processedDate, f.createdBy " +
           "ORDER BY f.batchSessionDate DESC, f.processedDate DESC")
    List<Object[]> findAllBatchSessionsSummary();
    
    @Query("SELECT DISTINCT f.batchSessionId, f.batchSessionDate, f.processedDate, COUNT(f) as recordCount, f.createdBy " +
           "FROM CbcFinalRecordEntity f " +
           "WHERE f.batchSessionDate BETWEEN :startDate AND :endDate " +
           "AND f.archiveStatus = 'ACTIVE' " +
           "GROUP BY f.batchSessionId, f.batchSessionDate, f.processedDate, f.createdBy " +
           "ORDER BY f.batchSessionDate DESC, f.processedDate DESC")
    List<Object[]> findBatchSessionsSummaryByDateRange(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT f.batchSessionId, f.batchSessionDate, f.processedDate, COUNT(f) as recordCount, f.createdBy " +
           "FROM CbcFinalRecordEntity f " +
           "WHERE LOWER(f.batchSessionId) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "AND f.archiveStatus = 'ACTIVE' " +
           "GROUP BY f.batchSessionId, f.batchSessionDate, f.processedDate, f.createdBy " +
           "ORDER BY f.batchSessionDate DESC, f.processedDate DESC")
    List<Object[]> findBatchSessionsSummaryBySearch(@Param("search") String search);
    
    @Query("SELECT f FROM CbcFinalRecordEntity f WHERE f.batchSessionId = :batchSessionId AND f.archiveStatus = 'ACTIVE'")
    List<CbcFinalRecordEntity> findByBatchSessionId(@Param("batchSessionId") String batchSessionId);
    
    @Query("SELECT f FROM CbcFinalRecordEntity f WHERE f.accountNumber = :accountNumber AND f.archiveStatus = 'ACTIVE'")
    List<CbcFinalRecordEntity> findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT f FROM CbcFinalRecordEntity f WHERE f.creditorId = :creditorId AND f.archiveStatus = 'ACTIVE'")
    List<CbcFinalRecordEntity> findByCreditorId(@Param("creditorId") String creditorId);
    
    @Query("SELECT f FROM CbcFinalRecordEntity f WHERE f.id IN :ids AND f.archiveStatus = 'ACTIVE'")
    List<CbcFinalRecordEntity> findByIds(@Param("ids") List<UUID> ids);
    
    @Modifying
    @Transactional
    @Query("UPDATE CbcFinalRecordEntity f SET f.archiveStatus = 'ARCHIVED', f.updatedBy = :updatedBy, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id IN :ids")
    void archiveRecords(@Param("ids") List<UUID> ids, @Param("updatedBy") String updatedBy);
    
    @Modifying
    @Transactional
    @Query("UPDATE CbcFinalRecordEntity f SET f.archiveStatus = 'DELETED', f.updatedBy = :updatedBy, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id IN :ids")
    void softDeleteRecords(@Param("ids") List<UUID> ids, @Param("updatedBy") String updatedBy);
    
    @Modifying
    @Transactional
    @Query("UPDATE CbcFinalRecordEntity f SET f.updateCount = f.updateCount + 1, f.lastUpdatedDate = CURRENT_TIMESTAMP, f.updatedBy = :updatedBy, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id IN :ids")
    void incrementUpdateCount(@Param("ids") List<UUID> ids, @Param("updatedBy") String updatedBy);
}