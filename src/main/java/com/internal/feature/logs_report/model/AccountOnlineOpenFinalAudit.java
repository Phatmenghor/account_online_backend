package com.internal.feature.logs_report.model;

import com.internal.config.entity.BaseNoIdEntity;
import com.internal.feature.auth.models.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "acc_online_audit_open_final"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineOpenFinalAudit extends BaseNoIdEntity {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;
    //ACCOUNT
    private String cif;
    // === Legal / NID Info ===
    @Column(name = "legal_id")
    private String legalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, columnDefinition = "uuid")
    private AccountOnlineFinal account;
}
