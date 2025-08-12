package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcLoanInformationDto {
    private Long id;
    private String loanTermType;
    private String groupAccountReference;
    private String dateIssued;
    private String productType;
    private String currency;
    private BigDecimal productLimitOriginalAmount;
    private String productExpiryDate;
    private String productStatus;
    private String restructuredLoan;
    private BigDecimal instalmentAmount;
    private String paymentFrequency;
    private String tenure;
    private String lastPaymentDate;
    private BigDecimal lastAmountPaid;
    private BigDecimal outstandingBalance;
    private BigDecimal pastDue;
    private String nextPaymentDate;
    private String paymentStatusCode;
    private String lossStatus;
    private String lossStatusDate;
    private String originalAmountAsAtLoadDate;
    private String emzOutstandingBalance;
}