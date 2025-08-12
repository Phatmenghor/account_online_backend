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
@Table(name = "staging_cbc_address_information")
public class CbcAddressInformationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "main_record_id")
    private CbcMainRecordEntity mainRecord;

    // Address 1
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

    // Address 2
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

    // Address 3
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
}