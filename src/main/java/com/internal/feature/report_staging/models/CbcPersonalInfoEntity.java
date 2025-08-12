package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staging_cbc_personal_info")
public class CbcPersonalInfoEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "main_record_id")
    private CbcMainRecordEntity mainRecord;

    @Column(name = "date_of_birth")
    private String dateOfBirth;
    
    @Column(name = "family_name_english")
    private String familyNameEnglish;
    
    @Column(name = "first_name_english")
    private String firstNameEnglish;
    
    @Column(name = "second_name_english")
    private String secondNameEnglish;
    
    @Column(name = "third_name_english")
    private String thirdNameEnglish;
    
    @Column(name = "unformatted_name_english")
    private String unformattedNameEnglish;
    
    @Column(name = "mothers_name_unformatted_english")
    private String mothersNameUnformattedEnglish;
    
    @Column(name = "family_name_khmer")
    private String familyNameKhmer;
    
    @Column(name = "first_name_khmer")
    private String firstNameKhmer;
    
    @Column(name = "second_name_khmer")
    private String secondNameKhmer;
    
    @Column(name = "third_name_khmer")
    private String thirdNameKhmer;
    
    @Column(name = "unformatted_name_khmer")
    private String unformattedNameKhmer;
    
    @Column(name = "mothers_name_unformatted_khmer")
    private String mothersNameUnformattedKhmer;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "marital_status")
    private String maritalStatus;
    
    @Column(name = "nationality_code")
    private String nationalityCode;
    
    @Column(name = "taxpayer_registration_number")
    private String taxpayerRegistrationNumber;
    
    @Column(name = "applicant_type")
    private String applicantType;
}