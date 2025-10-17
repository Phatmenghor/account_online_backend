package com.internal.feature.reference.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.internal.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OccupationDto {
    private Long id;
    private String nameEn;
    private String nameKh;
    private StatusData status;
}