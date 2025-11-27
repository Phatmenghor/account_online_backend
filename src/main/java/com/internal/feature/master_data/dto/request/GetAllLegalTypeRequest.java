package com.internal.feature.master_data.dto.request;

import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllLegalTypeRequest {
    @Builder.Default
    private int pageNo = 1;

    @Builder.Default
    private int pageSize = 10;
    private String search;
    private StatusData status = StatusData.ACTIVE;
}
