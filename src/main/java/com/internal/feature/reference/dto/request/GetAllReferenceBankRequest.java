package com.internal.feature.reference.dto.request;

import com.internal.enumation.LanguageEnum;
import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAllReferenceBankRequest {

    @Builder.Default
    private int pageNo = 1;

    @Builder.Default
    private int pageSize = 10;

    private LanguageEnum language;
    private String search;
    private StatusData status = StatusData.ACTIVE;
}
