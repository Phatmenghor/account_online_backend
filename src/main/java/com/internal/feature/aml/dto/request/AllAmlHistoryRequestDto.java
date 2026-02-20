
package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllAmlHistoryRequestDto {

    @Schema(example = "1", defaultValue = "1")
    private int pageNo = 1;

    @Schema(example = "10", defaultValue = "10")
    private int pageSize = 10;

    private String search;
    private AmlStatusEnum status;
    private LocalDate startDate;
    private LocalDate endDate;
}
