package com.internal.feature.open_account.models;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.AccountOpeningRequestStatusEnum;
import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import lombok.*;

import javax.persistence.*;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_pending_account_opening", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"legalId", "status"}, name = "uk_legal_id_pending_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAccountOpeningRequest extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String legalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountOpeningRequestStatusEnum status;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String requestData;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String customerInfo;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String amlResultData;

    @Enumerated(EnumType.STRING)
    private AmlStatusEnum amlStatus;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 255)
    private String rejectedBy;

    @Column(length = 500)
    private String approvalRemark;

    @Column(length = 255)
    private String approvedBy;

    private Long approvedAt;

    private Long rejectedAt;
}
