package com.internal.feature.auth.dto.request;

import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetAllUserRequestDto {

    @Builder.Default
    private int pageNo = 1;

    @Builder.Default
    private int pageSize = 10;

    private String search;
    private StatusData status = StatusData.ACTIVE;
}
