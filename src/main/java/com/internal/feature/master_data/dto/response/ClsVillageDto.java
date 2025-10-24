package com.internal.feature.master_data.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClsVillageDto {
    private String villageCode;
    private String villageEn;
    private String villageKh;
    private String communeCode; // commune code
}
