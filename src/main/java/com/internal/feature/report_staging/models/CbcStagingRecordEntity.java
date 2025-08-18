
package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staging_cbc_records")
public class CbcStagingRecordEntity extends BaseEntity {

    // Main Record Fields
    @Column(name = "account_number")
    private String accountNumber;
    @Column(name = "creditor_id")
    private String creditorId;
    @Column(name = "account_type")
    private String accountType;
    @Column(name = "as_of_date")
    private String asOfDate;


    // Status tracking
    @Column(name = "is_updated")
    private Boolean isUpdated = false;
}