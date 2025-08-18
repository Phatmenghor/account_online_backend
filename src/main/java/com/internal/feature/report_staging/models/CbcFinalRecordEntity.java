package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "final_cbc_records")
public class CbcFinalRecordEntity extends BaseEntity {

    // Batch session info for history tracking
    @Column(name = "batch_session_id")
    private String batchSessionId;
    
    @Column(name = "batch_session_date")
    private LocalDate batchSessionDate;
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "original_load_date")
    private LocalDate originalLoadDate;

    // History tracking
    @Column(name = "was_updated")
    private Boolean wasUpdated = false;
    
    @Column(name = "update_count")
    private Integer updateCount = 0;
    
    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;
    
    @Column(name = "archive_status")
    private String archiveStatus = "ACTIVE"; // ACTIVE, ARCHIVED, DELETED
}