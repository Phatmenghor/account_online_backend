package com.internal.feature.logs_report.model;

import com.internal.config.entity.BaseNoIdEntity;
import com.internal.enumation.AmlStatusEnum;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_open_final")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineFinal extends BaseNoIdEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    // === ACCOUNT INFO ===
    @Column(name = "cif")
    private String cif;

    @Column(name = "khr_account")
    private String khrAccount;

    @Column(name = "usd_account")
    private String usdAccount;

    @Column(name = "mnemonic")
    private String mnemonic;

    // === LEGAL / NID INFO ===
    @Column(name = "legal_id", nullable = false)
    private String legalId;

    @Column(name = "legal_doc_name")
    private String legalDocName;

    @Column(name = "legal_holder_name")
    private String legalHolderName;

    @Column(name = "legal_first_name_en")
    private String legalFirstNameEn;

    @Column(name = "legal_last_name_en")
    private String legalLastNameEn;

    @Column(name = "legal_first_name_kh")
    private String legalFirstNameKh;

    @Column(name = "legal_last_name_kh")
    private String legalLastNameKh;

    @Column(name = "legal_date_of_birth")
    private LocalDate legalDateOfBirth;

    @Column(name = "legal_gender")
    private String legalGender;

    @Column(name = "legal_address")
    private String legalAddress;

    @Column(name = "legal_place_of_birth")
    private String legalPlaceOfBirth;

    @Column(name = "legal_issued_date")
    private LocalDate legalIssuedDate;

    @Column(name = "legal_expired_date")
    private LocalDate legalExpiredDate;

    @Column(name = "legal_mrz1")
    private String legalMRZ1;

    @Column(name = "legal_mrz2")
    private String legalMRZ2;

    @Column(name = "legal_mrz3")
    private String legalMRZ3;

    // === CUSTOMER INFO ===
    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "average_income")
    private String averageIncome;

    @Column(name = "referral_id")
    private String referralId;

    @Column(name = "released_by")
    private String releasedBy;

    // === BRANCH INFO ===
    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "branch_name_kh")
    private String branchNameKh;

    // === CURRENT ADDRESS ===
    @Column(name = "customer_province_code")
    private String customerProvinceCode;

    @Column(name = "customer_province")
    private String customerProvince;

    @Column(name = "customer_district_code")
    private String customerDistrictCode;

    @Column(name = "customer_district")
    private String customerDistrict;

    @Column(name = "customer_commune_code")
    private String customerCommuneCode;

    @Column(name = "customer_commune")
    private String customerCommune;

    @Column(name = "customer_village_code")
    private String customerVillageCode;

    @Column(name = "customer_village")
    private String customerVillage;

    // === PLACE OF BIRTH ===
    @Column(name = "customer_pob_province_code")
    private String customerPobProvinceCode;

    @Column(name = "customer_pob_province")
    private String customerPobProvince;

    @Column(name = "customer_pob_district_code")
    private String customerPobDistrictCode;

    @Column(name = "customer_pob_district")
    private String customerPobDistrict;

    @Column(name = "customer_pob_commune_code")
    private String customerPobCommuneCode;

    @Column(name = "customer_pob_commune")
    private String customerPobCommune;

    @Column(name = "customer_pob_village_code")
    private String customerPobVillageCode;

    @Column(name = "customer_pob_village")
    private String customerPobVillage;

    // === CONTACT INFO ===
    @Column(name = "phone_number")
    private String phoneNumber;

    // === AML FINAL STATUS ===
    @Column(name = "aml_status")
    @Enumerated(EnumType.STRING)
    private AmlStatusEnum amlStatus;

    @Column(name = "aml_action_by")
    private Long amlActionBy; // user_id of last approver/rejector

    @Column(name = "aml_action_name")
    private String amlActionName; // full name for reporting

    @Column(name = "aml_action_role")
    private String amlActionRole; // optional

    @Column(name = "aml_remarks", columnDefinition = "TEXT")
    private String amlRemarks;

    @Column(name = "aml_screening_result", columnDefinition = "TEXT")
    private String amlScreeningResult;

    // === AML extracted fields ===
    @Column(name = "aml_risk_level")
    private String amlRiskLevel;

    @Column(name = "aml_action_taken")
    private String amlActionTaken;

    @Column(name = "aml_total_rules_score")
    private Integer amlTotalRulesScore;

    @Column(name = "aml_trxn_id")
    private String amlTrxnId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "aml_rules_triggered", columnDefinition = "TEXT")
    private String amlRulesTriggered;

    // === IMAGES ===
    @Column(name = "nid_image_name")
    private String nidImageName;

    @Column(name = "selfie_image_name")
    private String selfieImageName;

    // === SMS NOTIFICATION HISTORY ===
    @Column(name = "sms_sent_phone")
    private String smsSentPhone;

    @Column(name = "sms_sent_usd_account")
    private String smsSentUsdAccount;

    @Column(name = "sms_sent_khr_account")
    private String smsSentKhrAccount;

    @Column(name = "sms_sent_cif")
    private String smsSentCif;

    @Column(name = "mb_activation_code")
    private String mbActivationCode;

    @Column(name = "mb_app_download_link")
    private String mbAppDownloadLink;
}
