package com.internal.feature.logs_report.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomerFileUploadRequestDto {

    @NotNull(message = "legal id is required")
    private String legal_id;

    @NotNull(message = "Nid Image is required")
    private String NidImage; // uploaded file

    @NotNull(message = "Selfie Image is required")
    private String SelfieImage; // uploaded file

}
