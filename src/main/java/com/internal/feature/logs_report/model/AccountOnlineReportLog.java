package com.internal.feature.logs_report.model;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.OpenAccStatusEnum;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_report_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineReportLog extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Enumerated(EnumType.STRING)
    private OpenAccStatusEnum status;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
