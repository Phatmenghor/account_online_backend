package com.internal.feature.aml.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalAmlStatusUpdateDto {
    @NotBlank(message = "customerId (Legal ID) is required")
    private String customerId;
    private String updateFrom;
}
