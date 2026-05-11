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
public class RejectAccountOpeningRequestDto {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotNull(message = "Rejection reason is required")
    private String rejectionReason;
}
