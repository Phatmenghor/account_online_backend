package com.internal.feature.master_data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClsVillageDto {
    private String villageCode;
    private String villageDesc;
    private String villageDesc2;
    private String parentCode; // commune code
}
