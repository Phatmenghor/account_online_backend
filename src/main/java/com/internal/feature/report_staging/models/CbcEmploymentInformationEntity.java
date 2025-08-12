package com.internal.feature.report_staging.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staging_cbc_employment_information")
public class CbcEmploymentInformationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "main_record_id")
    private CbcMainRecordEntity mainRecord;

    // Employment 1
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

    // Employment 2
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

    // Employment 3
    @Column(name = "employer_type_3")
    private String employerType3;
    
    @Column(name = "self_employed_3")
    private String selfEmployed3;
    
    @Column(name = "employer_3_name_english")
    private String employer3NameEnglish;
}