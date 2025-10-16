package com.internal.feature.openAcc.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OtpRecord {
    private int id;
    private int attempt;
    private LocalDateTime lastAttempt;
    private Integer otpCode;
}