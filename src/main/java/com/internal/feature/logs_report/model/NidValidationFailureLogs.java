package com.internal.feature.logs_report.model;

import com.internal.config.BaseEntity;
import com.internal.enumation.OpenAccStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "nid_validation_failure_logs")
@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NidValidationFailureLogs extends BaseEntity {

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
}
