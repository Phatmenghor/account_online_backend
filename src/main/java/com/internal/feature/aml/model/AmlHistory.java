package com.internal.feature.aml.model;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.models.UserEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "acc_online_aml_history")
@Data
public class AmlHistory extends BaseEntity {

    @Column(name = "original_request")
    private String originalRequest;

    @Column(name = "original_response")
    private String originalResponse;

    @Enumerated(EnumType.STRING)
    private AmlStatusEnum oldStatus;

    @Enumerated(EnumType.STRING)
    private AmlStatusEnum newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private UserEntity changedBy;
}
