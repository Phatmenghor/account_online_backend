package com.internal.feature.master_data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllMasterDataRequest {
    private int pageNo = 1;
    private int pageSize = 20;
    private String search;  // Optional search filter
}
