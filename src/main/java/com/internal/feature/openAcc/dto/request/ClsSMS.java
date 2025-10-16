package com.internal.feature.openAcc.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClsSMS {
    private String phone;
    private String app;
    private String text;
}