package com.internal.feature.logs_report.dob.request;

import com.internal.enumation.OpenAccStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterNidValidationLogsDto {

    @NotNull(message = "From Date is required")
    private LocalDate fromDate;

    @NotNull(message = "To Date is required")
    private LocalDate toDate;

    @NotNull(message = "Password is required")
    private String password;

    private List<OpenAccStatusEnum> status;
}
