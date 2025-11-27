package com.internal.feature.master_data.dto.request;

import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OccupationUpdateRequestDto {
    private String nameEn;
    private String nameKh;
    private String occupationCode;
    private StatusData status;
}
