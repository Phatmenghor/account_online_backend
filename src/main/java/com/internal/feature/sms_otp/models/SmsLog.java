package com.internal.feature.sms_otp.models;

import com.internal.config.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sms_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsLog extends BaseEntity {

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "status", length = 20)
    private String status; // SUCCESS / FAILED

    @Column(name = "response_code", length = 20)
    private String responseCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}