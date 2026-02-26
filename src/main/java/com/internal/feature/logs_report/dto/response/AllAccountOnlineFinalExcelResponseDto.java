package com.internal.feature.logs_report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllAccountOnlineFinalExcelResponseDto {
    private List<AccountOnlineFinalExcelResponseDto> content;
    private int countAll;
}
