package com.internal.feature.report_staging.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveToFinalRequestDto {
    private String batchSessionNote; // Optional note for the batch session
}
