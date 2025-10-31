package com.internal.feature.logs_report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerImageUploadResponseDto {
    private String nidImagePath;
    private String selfieImagePath;
}
