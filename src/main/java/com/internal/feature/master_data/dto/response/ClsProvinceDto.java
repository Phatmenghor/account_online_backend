package com.internal.feature.master_data.dto.response;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ClsProvinceDto {
    private String provinceCode;
    private String provinceDesc;
    private String provinceDesc2;
    private String parentCode;
}
