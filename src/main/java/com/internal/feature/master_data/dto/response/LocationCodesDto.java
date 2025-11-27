package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationCodesDto {
    private ClsProvinceDto province;
    private ClsDistrictDto district;
    private ClsCommuneDto commune;
    private ClsVillageDto village;
}
