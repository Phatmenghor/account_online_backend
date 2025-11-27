package com.internal.feature.master_data.dto.request;

import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAllReferenceRequest {

    @Builder.Default
    private int pageNo = 1;

    @Builder.Default
    private int pageSize = 10;

    private String search;
    private StatusData status;
}
