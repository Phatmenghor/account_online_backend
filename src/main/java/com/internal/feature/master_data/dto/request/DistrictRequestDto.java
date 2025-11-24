package com.internal.feature.master_data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictRequestDto {
    private String districtCode;
    private String districtEn;
    private String districtKh;
    private String provinceCode;
}
