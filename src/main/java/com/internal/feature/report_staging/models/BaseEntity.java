package com.internal.feature.report_staging.models;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personal Info Fields
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

    // ID Information Fields
    @Column(name = "id_type_1")
    private String idType1;
    @Column(name = "id_number_1")
    private String idNumber1;
    @Column(name = "id_expiry_date_1")
    private String idExpiryDate1;
    @Column(name = "id_type_2")
    private String idType2;
    @Column(name = "id_number_2")
    private String idNumber2;
    @Column(name = "id_expiry_date_2")
    private String idExpiryDate2;
    @Column(name = "id_type_3")
    private String idType3;
    @Column(name = "id_number_3")
    private String idNumber3;
    @Column(name = "id_expiry_date_3")
    private String idExpiryDate3;

    // Address Information Fields
    @Column(name = "address_type_1")
    private String addressType1;
    @Column(name = "province_1")
    private String province1;
    @Column(name = "district_1")
    private String district1;
    @Column(name = "commune_1")
    private String commune1;
    @Column(name = "village_1")
    private String village1;
    @Column(name = "address_1_field1_english")
    private String address1Field1English;
    @Column(name = "address_1_field2_english")
    private String address1Field2English;
    @Column(name = "address_1_field1_khmer")
    private String address1Field1Khmer;
    @Column(name = "address_1_field2_khmer")
    private String address1Field2Khmer;
    @Column(name = "city_1_english")
    private String city1English;
    @Column(name = "city_1_khmer")
    private String city1Khmer;
    @Column(name = "country_1")
    private String country1;
    @Column(name = "postal_code_1")
    private String postalCode1;

    @Column(name = "address_type_2")
    private String addressType2;
    @Column(name = "province_2")
    private String province2;
    @Column(name = "district_2")
    private String district2;
    @Column(name = "commune_2")
    private String commune2;
    @Column(name = "village_2")
    private String village2;
    @Column(name = "address_2_field1_english")
    private String address2Field1English;
    @Column(name = "address_2_field2_english")
    private String address2Field2English;
    @Column(name = "address_2_field1_khmer")
    private String address2Field1Khmer;
    @Column(name = "address_2_field2_khmer")
    private String address2Field2Khmer;
    @Column(name = "city_2_english")
    private String city2English;
    @Column(name = "city_2_khmer")
    private String city2Khmer;
    @Column(name = "country_2")
    private String country2;
    @Column(name = "postal_code_2")
    private String postalCode2;

    @Column(name = "address_3_type")
    private String address3Type;
    @Column(name = "province_3")
    private String province3;
    @Column(name = "district_3")
    private String district3;
    @Column(name = "commune_3")
    private String commune3;
    @Column(name = "village_3")
    private String village3;
    @Column(name = "address_3_field1_english")
    private String address3Field1English;
    @Column(name = "address_3_field2_english")
    private String address3Field2English;
    @Column(name = "address_3_field1_khmer")
    private String address3Field1Khmer;
    @Column(name = "address_3_field2_khmer")
    private String address3Field2Khmer;
    @Column(name = "city_3_english")
    private String city3English;
    @Column(name = "city_3_khmer")
    private String city3Khmer;
    @Column(name = "country_3")
    private String country3;
    @Column(name = "postal_code_3")
    private String postalCode3;

    // Contact Information Fields
    @Column(name = "email_address")
    private String emailAddress;
    @Column(name = "contact_number_type_1")
    private String contactNumberType1;
    @Column(name = "contact_number_country_code_1")
    private String contactNumberCountryCode1;
    @Column(name = "contact_number_area_1")
    private String contactNumberArea1;
    @Column(name = "contact_number_number_1")
    private String contactNumberNumber1;
    @Column(name = "contact_number_extension_1")
    private String contactNumberExtension1;
    @Column(name = "contact_number_type_2")
    private String contactNumberType2;
    @Column(name = "contact_number_country_code_2")
    private String contactNumberCountryCode2;
    @Column(name = "contact_number_area_2")
    private String contactNumberArea2;
    @Column(name = "contact_number_number_2")
    private String contactNumberNumber2;
    @Column(name = "contact_number_extension_2")
    private String contactNumberExtension2;
    @Column(name = "contact_number_type_3")
    private String contactNumberType3;
    @Column(name = "contact_number_country_code_3")
    private String contactNumberCountryCode3;
    @Column(name = "contact_number_area_3")
    private String contactNumberArea3;
    @Column(name = "contact_number_number_3")
    private String contactNumberNumber3;
    @Column(name = "contact_number_extension_3")
    private String contactNumberExtension3;

    // Employment Information Fields
    @Column(name = "employment_status_1")
    private String employmentStatus1;
    @Column(name = "employment_type_1")
    private String employmentType1;
    @Column(name = "employer_1_name_english")
    private String employer1NameEnglish;
    @Column(name = "employer_1_name_khmer")
    private String employer1NameKhmer;
    @Column(name = "economic_sector_1")
    private String economicSector1;
    @Column(name = "business_type_1")
    private String businessType1;
    @Column(name = "employer_1_address_english")
    private String employer1AddressEnglish;
    @Column(name = "employer_1_address_khmer")
    private String employer1AddressKhmer;
    @Column(name = "employer_1_province")
    private String employer1Province;
    @Column(name = "employer_1_district")
    private String employer1District;
    @Column(name = "employer_1_commune")
    private String employer1Commune;
    @Column(name = "employer_1_village")
    private String employer1Village;
    @Column(name = "employer_1_address_city_english")
    private String employer1AddressCityEnglish;
    @Column(name = "employer_1_address_city_khmer")
    private String employer1AddressCityKhmer;
    @Column(name = "emp1_country_1")
    private String emp1Country1;
    @Column(name = "emp1_postal_code_1")
    private String emp1PostalCode1;
    @Column(name = "occupation_1_english")
    private String occupation1English;
    @Column(name = "occupation_1_khmer")
    private String occupation1Khmer;
    @Column(name = "date_of_employment_1")
    private String dateOfEmployment1;
    @Column(name = "length_of_service_1_months")
    private String lengthOfService1Months;
    @Column(name = "contract_expiry_date_1")
    private String contractExpiryDate1;
    @Column(name = "currency_1")
    private String currency1;
    @Column(name = "monthly_basic_salary_income_1", precision = 18, scale = 2)
    private BigDecimal monthlyBasicSalaryIncome1;
    @Column(name = "total_monthly_salary_income_1", precision = 18, scale = 2)
    private BigDecimal totalMonthlySalaryIncome1;

    @Column(name = "employer_type_2")
    private String employerType2;
    @Column(name = "self_employed_2")
    private String selfEmployed2;
    @Column(name = "employer_2_name_english")
    private String employer2NameEnglish;
    @Column(name = "employer_2_name_khmer")
    private String employer2NameKhmer;
    @Column(name = "economic_sector_2")
    private String economicSector2;
    @Column(name = "business_type_2")
    private String businessType2;
    @Column(name = "employer_2_address_english")
    private String employer2AddressEnglish;
    @Column(name = "employer_2_address_khmer")
    private String employer2AddressKhmer;
    @Column(name = "employer_2_province")
    private String employer2Province;
    @Column(name = "employer_2_district")
    private String employer2District;
    @Column(name = "employer_2_commune")
    private String employer2Commune;
    @Column(name = "employer_2_village")
    private String employer2Village;
    @Column(name = "employer_2_address_city_english")
    private String employer2AddressCityEnglish;
    @Column(name = "employer_2_address_city_khmer")
    private String employer2AddressCityKhmer;
    @Column(name = "emz_country_2")
    private String emzCountry2;
    @Column(name = "emz_postal_code_2")
    private String emzPostalCode2;
    @Column(name = "occupation_2_english")
    private String occupation2English;
    @Column(name = "occupation_2_khmer")
    private String occupation2Khmer;
    @Column(name = "date_of_employment_2")
    private String dateOfEmployment2;
    @Column(name = "length_of_service_2_months")
    private String lengthOfService2Months;
    @Column(name = "contract_expiry_date_2")
    private String contractExpiryDate2;
    @Column(name = "currency_2")
    private String currency2;
    @Column(name = "monthly_basic_salary_income_2", precision = 18, scale = 2)
    private BigDecimal monthlyBasicSalaryIncome2;
    @Column(name = "total_monthly_salary_income_2", precision = 18, scale = 2)
    private BigDecimal totalMonthlySalaryIncome2;

    @Column(name = "employer_type_3")
    private String employerType3;
    @Column(name = "self_employed_3")
    private String selfEmployed3;
    @Column(name = "employer_3_name_english")
    private String employer3NameEnglish;

    // Security Information Fields
    @Column(name = "security_type_1")
    private String securityType1;
    @Column(name = "security_number_1")
    private String securityNumber1;
    @Column(name = "security_currency_1")
    private String securityCurrency1;
    @Column(name = "security_value_1", precision = 18, scale = 2)
    private BigDecimal securityValue1;
    @Column(name = "security_location_1")
    private String securityLocation1;

    @Column(name = "security_type_2")
    private String securityType2;
    @Column(name = "security_number_2")
    private String securityNumber2;
    @Column(name = "security_currency_2")
    private String securityCurrency2;
    @Column(name = "security_value_2", precision = 18, scale = 2)
    private BigDecimal securityValue2;
    @Column(name = "security_location_2")
    private String securityLocation2;

    @Column(name = "security_type_3")
    private String securityType3;
    @Column(name = "security_number_3")
    private String securityNumber3;
    @Column(name = "security_currency_3")
    private String securityCurrency3;
    @Column(name = "security_value_3", precision = 18, scale = 2)
    private BigDecimal securityValue3;
    @Column(name = "security_location_3")
    private String securityLocation3;

    @Column(name = "security_type_primary")
    private String securityTypePrimary;
    @Column(name = "special_note")
    private String specialNote;
    @Column(name = "enquiry_member_reference")
    private String enquiryMemberReference;
    @Column(name = "loan_to_sector_section")
    private String loanToSectorSection;
    @Column(name = "currency_3")
    private String currency3;
    @Column(name = "branch_address_code")
    private String branchAddressCode;

    // Loan Information Fields
    @Column(name = "loan_term_type")
    private String loanTermType;
    @Column(name = "group_account_reference")
    private String groupAccountReference;
    @Column(name = "date_issued")
    private String dateIssued;
    @Column(name = "product_type")
    private String productType;
    @Column(name = "currency")
    private String currency;
    @Column(name = "product_limit_original_amount", precision = 18, scale = 2)
    private BigDecimal productLimitOriginalAmount;
    @Column(name = "product_expiry_date")
    private String productExpiryDate;
    @Column(name = "product_status")
    private String productStatus;
    @Column(name = "restructured_loan")
    private String restructuredLoan;
    @Column(name = "instalment_amount", precision = 18, scale = 2)
    private BigDecimal instalmentAmount;
    @Column(name = "payment_frequency")
    private String paymentFrequency;
    @Column(name = "tenure")
    private String tenure;
    @Column(name = "last_payment_date")
    private String lastPaymentDate;
    @Column(name = "last_amount_paid", precision = 18, scale = 2)
    private BigDecimal lastAmountPaid;
    @Column(name = "outstanding_balance", precision = 18, scale = 2)
    private BigDecimal outstandingBalance;
    @Column(name = "past_due", precision = 18, scale = 2)
    private BigDecimal pastDue;
    @Column(name = "next_payment_date")
    private String nextPaymentDate;
    @Column(name = "payment_status_code")
    private String paymentStatusCode;
    @Column(name = "loss_status")
    private String lossStatus;
    @Column(name = "loss_status_date")
    private String lossStatusDate;
    @Column(name = "original_amount_as_at_load_date")
    private String originalAmountAsAtLoadDate;
    @Column(name = "emz_outstanding_balance")
    private String emzOutstandingBalance;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
}
