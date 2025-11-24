package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillageResponseDto {
    private String villageCode;
    private String villageEn;
    private String villageKh;
    private CommuneResponseDto commune;
}
