package com.internal.feature.reference.dto.request;

import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalTypeUpdateRequestDto {
    private String nameEn;
    private String nameKh;
    private String legalTypeValue;
    private StatusData status;
}
