package com.internal.feature.logs_report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "acc_online_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
