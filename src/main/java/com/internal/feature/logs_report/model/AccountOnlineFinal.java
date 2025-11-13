package com.internal.feature.logs_report.model;

import com.internal.config.entity.BaseNoIdEntity;
import com.internal.enumation.AmlStatusEnum;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "acc_online_open_final"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineFinal extends BaseNoIdEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    //
    private String cif;
    private String khrAccount;
    private String usdAccount;
    private String mnemonic;

    // === Legal / NID Info ===
    @Column(name = "legal_id", nullable = false)
    private String legalId;

    @Column(name = "legal_doc_name")
    private String legalDocName; // e.g., NATIONAL.ID

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

    // === Customer Info ===
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

    // === Branch Info ===
    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "branch_name_en")
    private String branchNameEn;

    @Column(name = "branch_name_kh")
    private String branchNameKh;

    // === Current Address Codes + Names ===

    // Province
    @Column(name = "customer_province_code")
    private String customerProvinceCode;

    @Column(name = "customer_province")
    private String customerProvince;

    // District
    @Column(name = "customer_district_code")
    private String customerDistrictCode;

    @Column(name = "customer_district")
    private String customerDistrict;

    // Commune
    @Column(name = "customer_commune_code")
    private String customerCommuneCode;

    @Column(name = "customer_commune")
    private String customerCommune;

    // Village
    @Column(name = "customer_village_code")
    private String customerVillageCode;

    @Column(name = "customer_village")
    private String customerVillage;

    // === Place of Birth (POB) Codes + Names ===

    // Province
    @Column(name = "customer_pob_province_code")
    private String customerPobProvinceCode;

    @Column(name = "customer_pob_province")
    private String customerPobProvince;

    // District
    @Column(name = "customer_pob_district_code")
    private String customerPobDistrictCode;

    @Column(name = "customer_pob_district")
    private String customerPobDistrict;

    // Commune
    @Column(name = "customer_pob_commune_code")
    private String customerPobCommuneCode;

    @Column(name = "customer_pob_commune")
    private String customerPobCommune;

    // Village
    @Column(name = "customer_pob_village_code")
    private String customerPobVillageCode;

    @Column(name = "customer_pob_village")
    private String customerPobVillage;

    // === Contact ===
    @Column(name = "phone_number")
    private String phoneNumber;

    // === AML Info ===
    @Column(name = "aml_status")
    @Enumerated(EnumType.STRING)
    private AmlStatusEnum amlStatus; // APPROVED / REJECTED / PENDING

    @Column(name = "aml_approved_by")
    private UUID amlApprovedById;

    @Column(name = "aml_rejected_by")
    private UUID amlRejectedById;

    @Column(name = "aml_remarks", columnDefinition = "TEXT")
    private String amlRemarks;

    @Column(name = "aml_screening_result", columnDefinition = "TEXT")
    private String amlScreeningResult; // JSON from AML service

    // === Individual fields from AML JSON for easier queries ===
    @Column(name = "aml_risk_level")
    private String amlRiskLevel;

    @Column(name = "aml_action_taken")
    private String amlActionTaken;

    @Column(name = "aml_total_rules_score")
    private Integer amlTotalRulesScore;

    @Column(name = "aml_trxn_id")
    private String amlTrxnId;

    // For storing triggered rules as comma-separated string
    @Column(name = "aml_rules_triggered", columnDefinition = "TEXT")
    private String amlRulesTriggered;

    @Column(name = "nid_image")
    private String nidImage;

    @Column(name = "selfie_image")
    private String selfieImage;
}
