package com.internal.feature.logs_report.model;

import com.internal.config.entity.BaseNoIdEntity;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_open")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineSuccessLog extends BaseNoIdEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String recId;
    private String legalId;
    private String familyName;
    private String givenName;
    private String firstNameKh;
    private String lastNameKh;
    private String dateOfBirth;
    private String legalAddress;
    private String gender;
    private String maritalStatus;
    private String companyName;
    private String referralId;
    private String branchCode;
    private String placeOfBirth;
    private String nationality;
    private String releasedBy;
    private String averageIncome;
    private String legalDocName;
    private String occupation;
    private String customerProvince;
    private String customerDistrict;
    private String customerCommune;
    private String customerVillage;
    private String phoneNumber;

    private String nidImage;
    private String selfieImage;
}
