package com.internal.feature.open_account.dto.request;

import lombok.Data;

@Data
public class Base64UploadRequest {
    private String fileBase64;
    private String fileName;
    private String type;
    private String legalId;
}
