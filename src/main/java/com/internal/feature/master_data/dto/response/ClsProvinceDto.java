package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClsProvinceDto {
    private String provinceCode;
    private String provinceDesc;
    private String provinceDesc2;
    private String parentCode;
}
