package com.internal.feature.logs_report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineFinalExcelResponseDto {

    private UUID id;

    // === ACCOUNT INFO ===
    private String cif;
    private String khrAccount;
    private String usdAccount;
    private String mnemonic;

    // === LEGAL / NID INFO ===
    private String legalId;

    // === BRANCH INFO ===
    private String branchCode;
    private String branchNameKh;

    // === IMAGES ===
    private String nidImageName;
    private String selfieImageName;

    // === TRACE FIELDS FROM BaseNoIdEntity ===
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
