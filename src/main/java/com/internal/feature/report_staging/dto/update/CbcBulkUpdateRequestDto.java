package com.internal.feature.report_staging.dto.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcBulkUpdateRequestDto {
    
    @NotEmpty(message = "Update requests cannot be empty")
    @Valid
    private List<CbcUpdateRequestDto> updates;
    
    private String batchNote;
    private boolean validateBeforeUpdate = true;
    private boolean continueOnError = false;
}