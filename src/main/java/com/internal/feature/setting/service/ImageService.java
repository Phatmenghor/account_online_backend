package com.internal.feature.setting.service;

import com.internal.feature.setting.dto.ImageDto;
import com.internal.feature.setting.dto.ImageResponse;
import com.internal.feature.setting.dto.ImageUploadRequest;

import java.util.UUID;

public interface ImageService {

    ImageDto uploadImage(ImageUploadRequest request);

    ImageResponse getImageById(UUID id);

    void deleteImage(UUID id);
}
