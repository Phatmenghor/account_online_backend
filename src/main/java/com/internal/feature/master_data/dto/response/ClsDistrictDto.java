package com.internal.feature.master_data.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClsDistrictDto {
    private String districtCode;
    private String districtEn;
    private String districtKh;
    private String provinceCode;
}
