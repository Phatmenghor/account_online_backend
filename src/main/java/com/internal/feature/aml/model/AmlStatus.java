package com.internal.feature.aml.model;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.models.UserEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "acc_online_aml_status")
@Data
public class AmlStatus extends BaseEntity {

    @Column(name = "original_request")
    private String originalRequest;

    @Column(name = "original_response")
    private String originalResponse;

    @Enumerated(EnumType.STRING)
    private AmlStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserEntity approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private UserEntity rejectedBy;

    //customer info
    private String idDisplay;
    private String familyName;
    private String givenName;
    private String firstNameKh;
    private String lastNameKh;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String legalAddress;

}
