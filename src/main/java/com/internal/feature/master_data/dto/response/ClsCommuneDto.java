package com.internal.feature.master_data.dto.response;

import lombok.Data;

@Data
public class ClsCommuneDto {
    private String communeCode;
    private String communeDesc;
    private String communeDesc2;
    private String parentCode; // district code
}
