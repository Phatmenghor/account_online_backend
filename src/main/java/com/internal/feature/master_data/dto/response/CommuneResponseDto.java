package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommuneResponseDto {
    private String communeCode;
    private String communeEn;
    private String communeKh;
    private DistrictResponseDto district;
}
