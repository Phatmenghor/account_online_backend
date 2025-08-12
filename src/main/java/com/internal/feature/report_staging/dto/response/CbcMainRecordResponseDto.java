
package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcMainRecordResponseDto {
    private Long id;
    private String batchId;
    private LocalDate requestStartDate;
    private LocalDate requestEndDate;
    private LocalDate processingDate;
    private String accountNumber;
    private String creditorId;
    private String accountType;
    private String asOfDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Related information
    private CbcPersonalInfoDto personalInfo;
    private CbcIdInformationDto idInformation;
    private CbcAddressInformationDto addressInformation;
    private CbcContactInformationDto contactInformation;
    private CbcEmploymentInformationDto employmentInformation;
    private CbcSecurityInformationDto securityInformation;
    private CbcLoanInformationDto loanInformation;
}