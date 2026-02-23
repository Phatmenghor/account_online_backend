package com.internal.feature.logs_report.service;

import com.internal.feature.logs_report.dto.request.CustomerFileUploadRequestDto;
import com.internal.feature.logs_report.dto.response.CustomerImageUploadResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerImageService {
    CustomerImageUploadResponseDto saveCustomerImages(CustomerFileUploadRequestDto request);

    // New method for direct file upload
    String saveUploadedFile(MultipartFile file, String filename) throws Exception;

    Resource getNidImageResourceForEmail(String customerId);

    String saveBase64File(String base64, String filename, String type) throws Exception;

    byte[] getNidImageBytes(String customerId);

    Resource getSelfieImageResourceForEmail(String customerId);

    byte[] getSelfieImageBytes(String customerId);

    boolean nidImageExists(String customerId);

    boolean selfieImageExists(String customerId);
}
