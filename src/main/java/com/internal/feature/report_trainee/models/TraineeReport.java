package com.internal.feature.report_trainee.models;

import com.internal.enumation.ApplicationStatusEnum;
import com.internal.feature.auth.models.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "trainee_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeReport extends BaseEntity {

    @Lob
    @Column(name = "reportRemark", columnDefinition = "TEXT")
    private String reportRemark;

    @Lob
    @Column(name = "challenge", columnDefinition = "TEXT")
    private String challenge;

    @Lob
    @Column(name = "recommend", columnDefinition = "TEXT")
    private String recommend;
}