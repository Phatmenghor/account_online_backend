package com.internal.feature.logs_report.model;

import com.internal.enumation.OpenAccStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nid_validation_failure_logs")
@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NidValidationFailureLogs {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "original_request", columnDefinition = "TEXT")
        private String request;

    @Column(name = "original_response", columnDefinition = "TEXT")
    private String response;

    @Enumerated(EnumType.STRING)
    private OpenAccStatusEnum status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(nullable = true, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = true, length = 100)
    private String updatedBy;
}
