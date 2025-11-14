package com.internal.feature.logs_report.dto.request;

import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountOnlineFinalAuditRequestDto {

    private String cif;
    private String legalId;
    private UserEntity user;
    private AccountOnlineFinal account;
}
