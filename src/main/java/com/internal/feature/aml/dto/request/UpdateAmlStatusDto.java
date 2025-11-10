package com.internal.feature.aml.dto.request;

import com.internal.enumation.AmlStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAmlStatusDto {
    private AmlStatusEnum status;
}
