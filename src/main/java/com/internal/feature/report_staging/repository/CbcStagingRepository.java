package com.internal.feature.report_staging.repository;

import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public interface CbcStagingRepository extends JpaRepository<CbcStagingRecordEntity, UUID>,
        JpaSpecificationExecutor<CbcStagingRecordEntity> {
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CbcStagingRecordEntity")
    void deleteAllStaging();
    
    @Query("SELECT COUNT(s) FROM CbcStagingRecordEntity s")
    long countAllStaging();
    
    @Query("SELECT COUNT(s) FROM CbcStagingRecordEntity s WHERE s.isUpdated = true")
    long countUpdatedRecords();
    
    @Query("SELECT COUNT(s) FROM CbcStagingRecordEntity s WHERE s.validationStatus = :status")
    long countByValidationStatus(@Param("status") String status);
    
    @Query("SELECT s FROM CbcStagingRecordEntity s WHERE s.accountNumber = :accountNumber")
    List<CbcStagingRecordEntity> findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT s FROM CbcStagingRecordEntity s WHERE s.creditorId = :creditorId")
    List<CbcStagingRecordEntity> findByCreditorId(@Param("creditorId") String creditorId);
    
    @Query("SELECT s FROM CbcStagingRecordEntity s WHERE s.isUpdated = :isUpdated")
    List<CbcStagingRecordEntity> findByIsUpdated(@Param("isUpdated") Boolean isUpdated);
    
    @Query("SELECT s FROM CbcStagingRecordEntity s WHERE s.validationStatus = :status")
    List<CbcStagingRecordEntity> findByValidationStatus(@Param("status") String status);
    
    @Query("SELECT s FROM CbcStagingRecordEntity s WHERE s.id IN :ids")
    List<CbcStagingRecordEntity> findByIds(@Param("ids") List<UUID> ids);
    
    @Modifying
    @Transactional
    @Query("UPDATE CbcStagingRecordEntity s SET s.validationStatus = :status, s.validationErrors = :errors WHERE s.id = :id")
    void updateValidationStatus(@Param("id") UUID id, @Param("status") String status, @Param("errors") String errors);
    
    @Modifying
    @Transactional
    @Query("UPDATE CbcStagingRecordEntity s SET s.isUpdated = true, s.updatedBy = :updatedBy, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id IN :ids")
    void markAsUpdated(@Param("ids") List<UUID> ids, @Param("updatedBy") String updatedBy);
}