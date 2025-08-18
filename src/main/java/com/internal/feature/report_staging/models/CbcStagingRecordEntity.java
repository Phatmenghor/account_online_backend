package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staging_cbc_records")
public class CbcStagingRecordEntity extends BaseEntity {

    // Status tracking for staging records
    @Column(name = "is_updated")
    private Boolean isUpdated = false;
    
    @Column(name = "validation_status")
    private String validationStatus = "PENDING"; // PENDING, VALIDATED, INVALID
    
    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;
}