
package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "final_cbc_records")
public class CbcFinalRecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Batch session info for history tracking
    @Column(name = "batch_session_id")
    private String batchSessionId;
    
    @Column(name = "batch_session_date")
    private LocalDate batchSessionDate;
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "original_load_date")
    private LocalDate originalLoadDate;

    // All the same fields as staging table
    @Column(name = "account_number")
    private String accountNumber;
    @Column(name = "creditor_id")
    private String creditorId;
    @Column(name = "account_type")
    private String accountType;
    @Column(name = "as_of_date")
    private String asOfDate;

    // History tracking
    @Column(name = "was_updated")
    private Boolean wasUpdated = false;
}