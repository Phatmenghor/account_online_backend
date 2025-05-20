package com.account_sell.feature.setting.service;

import com.account_sell.feature.setting.dto.ImageDto;
import com.account_sell.feature.setting.dto.ImageResponse;
import com.account_sell.feature.setting.dto.ImageUploadRequest;

import java.util.UUID;

public interface ImageService {

    ImageDto uploadImage(ImageUploadRequest request);

    ImageResponse getImageById(UUID id);

    void deleteImage(UUID id);
}