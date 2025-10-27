package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllAmlRequestDto {
    @Builder.Default
    private int pageNo = 1;

    @Builder.Default
    private int pageSize = 10;
    private String search;
    private AmlStatusEnum status;
    private LocalDate startDate;
    private LocalDate endDate;
}
