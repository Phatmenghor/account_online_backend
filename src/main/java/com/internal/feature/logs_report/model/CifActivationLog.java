package com.internal.feature.logs_report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "acc_cif_activation_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CifActivationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "cif_no", length = 50)
    private String cifNo;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "account_no", length = 50)
    private String accountNo;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "branch_code", length = 20)
    private String branchCode;

    @Column(name = "channel", length = 100)
    private String channel;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "error_code", length = 20)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "activation_code", length = 100)
    private String activationCode;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
