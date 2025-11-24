package com.internal.feature.master_data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceRequestDto {
    private String provinceCode;
    private String provinceEn;
    private String provinceKh;
}
