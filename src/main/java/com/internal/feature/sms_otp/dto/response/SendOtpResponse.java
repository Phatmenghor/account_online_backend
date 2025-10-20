package com.internal.feature.sms_otp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendOtpResponse {

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Message to display")
    private String message;

    @Schema(description = "OTP expires at")
    private LocalDateTime expiresAt;
}
