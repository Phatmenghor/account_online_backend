package com.account_sell.feature.setting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ImageUploadRequest {

    @NotBlank(message = "Image type is required")
    private String type;

    @NotBlank(message = "Image data is required")
    private String base64;
}