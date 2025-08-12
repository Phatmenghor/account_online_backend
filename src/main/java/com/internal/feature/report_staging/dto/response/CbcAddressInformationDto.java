package com.internal.feature.report_staging.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcAddressInformationDto {
    private Long id;
    private String addressType1;
    private String province1;
    private String district1;
    private String commune1;
    private String village1;
    private String address1Field1English;
    private String address1Field2English;
    private String address1Field1Khmer;
    private String address1Field2Khmer;
    private String city1English;
    private String city1Khmer;
    private String country1;
    private String postalCode1;
    
    private String addressType2;
    private String province2;
    private String district2;
    private String commune2;
    private String village2;
    private String address2Field1English;
    private String address2Field2English;
    private String address2Field1Khmer;
    private String address2Field2Khmer;
    private String city2English;
    private String city2Khmer;
    private String country2;
    private String postalCode2;
    
    private String address3Type;
    private String province3;
    private String district3;
    private String commune3;
    private String village3;
    private String address3Field1English;
    private String address3Field2English;
    private String address3Field1Khmer;
    private String address3Field2Khmer;
    private String city3English;
    private String city3Khmer;
    private String country3;
    private String postalCode3;
}