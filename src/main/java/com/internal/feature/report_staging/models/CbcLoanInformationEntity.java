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
@Table(name = "staging_cbc_loan_information")
public class CbcLoanInformationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "main_record_id")
    private CbcMainRecordEntity mainRecord;

    @Column(name = "loan_term_type")
    private String loanTermType;
    
    @Column(name = "group_account_reference")
    private String groupAccountReference;
    
    @Column(name = "date_issued")
    private String dateIssued;
    
    @Column(name = "product_type")
    private String productType;
    
    @Column(name = "currency")
    private String currency;
    
    @Column(name = "product_limit_original_amount", precision = 18, scale = 2)
    private BigDecimal productLimitOriginalAmount;
    
    @Column(name = "product_expiry_date")
    private String productExpiryDate;
    
    @Column(name = "product_status")
    private String productStatus;
    
    @Column(name = "restructured_loan")
    private String restructuredLoan;
    
    @Column(name = "instalment_amount", precision = 18, scale = 2)
    private BigDecimal instalmentAmount;
    
    @Column(name = "payment_frequency")
    private String paymentFrequency;
    
    @Column(name = "tenure")
    private String tenure;
    
    @Column(name = "last_payment_date")
    private String lastPaymentDate;
    
    @Column(name = "last_amount_paid", precision = 18, scale = 2)
    private BigDecimal lastAmountPaid;
    
    @Column(name = "outstanding_balance", precision = 18, scale = 2)
    private BigDecimal outstandingBalance;
    
    @Column(name = "past_due", precision = 18, scale = 2)
    private BigDecimal pastDue;
    
    @Column(name = "next_payment_date")
    private String nextPaymentDate;
    
    @Column(name = "payment_status_code")
    private String paymentStatusCode;
    
    @Column(name = "loss_status")
    private String lossStatus;
    
    @Column(name = "loss_status_date")
    private String lossStatusDate;
    
    @Column(name = "original_amount_as_at_load_date")
    private String originalAmountAsAtLoadDate;
    
    @Column(name = "emz_outstanding_balance")
    private String emzOutstandingBalance;
}