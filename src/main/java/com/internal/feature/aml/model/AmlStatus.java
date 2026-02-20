package com.internal.feature.aml.model;

import com.internal.config.entity.BaseEntity;
import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.auth.models.UserEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_aml_status")
@Data
public class AmlStatus extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmlStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserEntity approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private UserEntity rejectedBy;

    // ============================================
    // CUSTOMER IDENTIFICATION
    // ============================================
    @Column(name = "legal_id", nullable = false, unique = true)
    private String legalId; // ID / Customer Number

    // ============================================
    // CUSTOMER NAME (ENGLISH)
    // ============================================
    @Column(name = "family_name")
    private String familyName; // Last Name (English)

    @Column(name = "given_name")
    private String givenName; // First Name (English)

    // ============================================
    // CUSTOMER NAME (KHMER)
    // ============================================
    @Column(name = "last_name_kh")
    private String lastNameKh; // Last Name (Khmer)

    @Column(name = "first_name_kh")
    private String firstNameKh; // First Name (Khmer)

    // ============================================
    // PERSONAL INFORMATION
    // ============================================
    @Column(name = "date_of_birth")
    private String dateOfBirth; // Date of Birth

    @Column(name = "gender")
    private String gender; // Gender

    @Column(name = "nationality")
    private String nationality; // Nationality

    @Column(name = "phone_number")
    private String phoneNumber; // Phone Number

    @Column(name = "marital_status")
    private String maritalStatus; // Marital Status

    // ============================================
    // DOCUMENT INFORMATION
    // ============================================
    @Column(name = "issued_date")
    private String issuedDate; // Issued Date

    @Column(name = "expired_date")
    private String expiredDate; // Expired Date

    // ============================================
    // CURRENT ADDRESS
    // ============================================
    @Column(name = "current_address_code")
    private String currentAddressCode; // Current Address Code

    @Column(name = "current_address_name", columnDefinition = "TEXT")
    private String currentAddressName; // Current Address Name (EN + KH)

    // ============================================
    // PLACE OF BIRTH
    // ============================================
    @Column(name = "place_of_birth_code")
    private String placeOfBirthCode; // Place of Birth Address Code

    @Column(name = "place_of_birth_name")
    private String placeOfBirthName; // Place of Birth Address Name (EN + KH)

    // ============================================
    // OCCUPATION
    // ============================================
    @Column(name = "occupation_code")
    private String occupationCode; // Occupation Code

    @Column(name = "occupation_status")
    private String occupationStatus; // Occupation Status

    // ============================================
    // AML SCREENING INFORMATION
    // ============================================
    @Column(name = "screening_result", columnDefinition = "TEXT")
    private String screeningResult; // JSON result from AML service

    @Column(name = "aml_ext_risk_level")
    private String amlExternalRiskLevel;

    @Column(name = "aml_ext_action_taken")
    private String  amlExternalActionTaken;

    @Column(name = "aml_ext_rules_triggered", columnDefinition = "TEXT")
    private String amlExternalRulesTriggered; // store as JSON string

    @Column(name = "aml_ext_service_name")
    private String amlExternalServiceName;

    @Column(name = "aml_ext_total_rules_score")
    private Integer amlExternalTotalRulesScore;

    @Column(name = "aml_ext_trxn_id")
    private String amlExternalTrxnID;

    // ============================================
    // IMAGE FILENAMES
    // ============================================
    @Column(name = "nid_image_name")
    private String nidImageName;

    @Column(name = "selfie_image_name")
    private String selfieImageName;

    // ============================================
    // ADMIN REMARKS
    // ============================================
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
