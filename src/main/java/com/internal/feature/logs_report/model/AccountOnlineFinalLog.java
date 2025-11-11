package com.internal.feature.logs_report.model;

import com.internal.config.entity.BaseNoIdEntity;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "acc_online_open_final")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOnlineFinalLog extends BaseNoIdEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    // === Request Info ===
    @Column(name = "rec_id")
    private String recId;

    // === Legal / NID Info ===
    @Column(name = "legal_id")
    private String legalId; // idNumber from NID

    @Column(name = "legal_doc_name")
    private String legalDocName; // e.g., NATIONAL.ID

    @Column(name = "legal_holder_name")
    private String legalHolderName; // full name on front of NID

    @Column(name = "legal_first_name_en")
    private String legalFirstNameEn; // firstNameEn from NID

    @Column(name = "legal_last_name_en")
    private String legalLastNameEn; // lastNameEn from NID

    @Column(name = "legal_first_name_kh")
    private String legalFirstNameKh; // firstNameKh from NID

    @Column(name = "legal_last_name_kh")
    private String legalLastNameKh; // lastNameKh from NID

    @Column(name = "legal_date_of_birth")
    private String legalDateOfBirth; // dob from NID

    @Column(name = "legal_gender")
    private String legalGender; // gender from NID (M/F)

    @Column(name = "legal_address")
    private String legalAddress; // address from NID

    @Column(name = "legal_place_of_birth")
    private String legalPlaceOfBirth; // pob from NID

    @Column(name = "legal_issued_date")
    private String legalIssuedDate; // issuedDate from NID

    @Column(name = "legal_expired_date")
    private String legalExpiredDate; // expiredDate from NID

    @Column(name = "legal_mrz1")
    private String legalMRZ1; // MRZ1 from NID

    @Column(name = "legal_mrz2")
    private String legalMRZ2; // MRZ2 from NID

    @Column(name = "legal_mrz3")
    private String legalMRZ3; // MRZ3 from NID

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

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "referral_id")
    private String referralId;

    @Column(name = "released_by")
    private String releasedBy;

    // === Address Codes ===
    @Column(name = "customer_province")
    private String customerProvince;

    @Column(name = "customer_district")
    private String customerDistrict;

    @Column(name = "customer_commune")
    private String customerCommune;

    @Column(name = "customer_village")
    private String customerVillage;

    // === Contact ===
    @Column(name = "phone_number")
    private String phoneNumber;

    // === Images (base64) ===
    @Lob
    @Column(name = "nid_image", columnDefinition = "TEXT")
    private String nidImage;

    @Lob
    @Column(name = "selfie_image", columnDefinition = "TEXT")
    private String selfieImage;
}
