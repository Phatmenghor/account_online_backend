package com.internal.feature.master_data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VillageRequestDto {
    private String villageCode;
    private String villageEn;
    private String villageKh;
    private String communeCode;
}
