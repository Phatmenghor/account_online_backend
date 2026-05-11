package com.internal.feature.open_account.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveAccountOpeningRequestDto {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    private String approvalRemark;
}
