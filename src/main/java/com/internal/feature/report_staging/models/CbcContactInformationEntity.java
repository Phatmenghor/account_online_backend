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
@Table(name = "staging_cbc_contact_information")
public class CbcContactInformationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "main_record_id")
    private CbcMainRecordEntity mainRecord;

    @Column(name = "email_address")
    private String emailAddress;
    
    // Contact Set 1
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
    
    // Contact Set 2
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
    
    // Contact Set 3
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
}
