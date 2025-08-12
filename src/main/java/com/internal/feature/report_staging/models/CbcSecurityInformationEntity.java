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
@Table(name = "staging_cbc_security_information")
public class CbcSecurityInformationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "main_record_id")
    private CbcMainRecordEntity mainRecord;

    // Security 1
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

    // Security 2
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

    // Security 3
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
}