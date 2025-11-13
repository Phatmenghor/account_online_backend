package com.internal.feature.logs_report.service;

import com.internal.feature.logs_report.dto.request.CustomerFileUploadRequestDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import org.springframework.core.io.Resource;

public interface CustomerImageService {
    CustomerImageUploadResponseDto saveCustomerImages(CustomerFileUploadRequestDto request);

    Resource getNidImageResourceForEmail(String customerId);
    byte[] getNidImageBytes(String customerId);

    Resource getSelfieImageResourceForEmail(String customerId);
    byte[] getSelfieImageBytes(String customerId);

    boolean nidImageExists(String customerId);

    boolean selfieImageExists(String customerId);
}
