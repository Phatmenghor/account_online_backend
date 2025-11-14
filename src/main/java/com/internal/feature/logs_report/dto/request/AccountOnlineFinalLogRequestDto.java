package com.internal.feature.logs_report.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountOnlineFinalLogRequestDto {
    private String cif;
    private String legalId;
}
