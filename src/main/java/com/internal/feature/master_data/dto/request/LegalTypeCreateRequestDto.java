package com.internal.feature.master_data.dto.request;

import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalTypeCreateRequestDto {
    @NotBlank(message = "English name must not be blank")
    private String nameEn;

    @NotBlank(message = "Khmer name must not be blank")
    private String nameKh;

    private String legalTypeValue;

    private StatusData status = StatusData.ACTIVE;
}
