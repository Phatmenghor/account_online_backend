package com.internal.feature.open_account.dto.response;

import com.internal.enumation.AccountOpeningRequestStatusEnum;
import com.internal.enumation.AmlStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAccountOpeningRequestDto {

    private Long id;
    private String legalId;
    private AccountOpeningRequestStatusEnum status;
    private AmlStatusEnum amlStatus;
    private String rejectionReason;
    private String rejectedBy;
    private String approvalRemark;
    private String approvedBy;
    private Long createdAt;
    private Long approvedAt;
    private Long rejectedAt;
}
