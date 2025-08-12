package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staging_cbc_main_record")
public class CbcMainRecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", unique = true)
    private String batchId;
    
    @Column(name = "request_start_date")
    private LocalDate requestStartDate;
    
    @Column(name = "request_end_date")
    private LocalDate requestEndDate;
    
    @Column(name = "processing_date")
    private LocalDate processingDate;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "creditor_id")
    private String creditorId;
    
    @Column(name = "account_type")
    private String accountType;
    
    @Column(name = "as_of_date")
    private String asOfDate;

    // One-to-One relationships
    @OneToOne(mappedBy = "mainRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CbcPersonalInfoEntity personalInfo;
    
    @OneToOne(mappedBy = "mainRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CbcIdInformationEntity idInformation;
    
    @OneToOne(mappedBy = "mainRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CbcAddressInformationEntity addressInformation;
    
    @OneToOne(mappedBy = "mainRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CbcContactInformationEntity contactInformation;
    
    @OneToOne(mappedBy = "mainRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CbcEmploymentInformationEntity employmentInformation;
    
    @OneToOne(mappedBy = "mainRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CbcSecurityInformationEntity securityInformation;
    
    @OneToOne(mappedBy = "mainRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CbcLoanInformationEntity loanInformation;
}
