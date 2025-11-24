package com.internal.feature.master_data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommuneRequestDto {
    private String communeCode;
    private String communeEn;
    private String communeKh;
    private String districtCode;
}
