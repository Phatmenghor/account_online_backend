package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponseDto {
    private Long id;
    private String districtCode;
    private String districtEn;
    private String districtKh;
    private ProvinceResponseDto province;
}
