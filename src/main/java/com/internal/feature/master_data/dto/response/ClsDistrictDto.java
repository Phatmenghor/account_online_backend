package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClsDistrictDto {
    private String DistrictCode;
    private String DistrictDesc;
    private String DistrictDesc2;
    private String ParentCode;
}
