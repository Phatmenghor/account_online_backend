package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceResponseDto {
    private Long id;
    private String provinceCode;
    private String provinceEn;
    private String provinceKh;
}
