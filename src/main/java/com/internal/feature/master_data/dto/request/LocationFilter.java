package com.internal.feature.master_data.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationFilter {
    private String provinceCode;
    private String districtCode;
    private String communeCode;
}
