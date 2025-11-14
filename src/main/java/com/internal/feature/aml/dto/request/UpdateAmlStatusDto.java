package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAmlStatusDto {
    @NotNull(message = "Status must not be null")
    private AmlStatusEnum status;
    private String remark;
}
