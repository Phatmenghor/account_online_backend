package com.internal.feature.setting.service.impl;

import com.internal.exceptions.error.NotFoundException;
import com.internal.feature.setting.dto.ImageDto;
import com.internal.feature.setting.dto.ImageResponse;
import com.internal.feature.setting.dto.ImageUploadRequest;
import com.internal.feature.setting.mapper.ImageMapper;
import com.internal.feature.setting.models.ImageEntity;
import com.internal.feature.setting.repository.ImageRepository;
import com.internal.feature.setting.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @Override
    @Transactional
    public ImageDto uploadImage(ImageUploadRequest request) {
        // Normalize the type field - add "image/" prefix if it doesn't have it
        if (!request.getType().contains("/")) {
            request.setType("image/" + request.getType());
        }

        ImageEntity image = imageMapper.toEntity(request);
        ImageEntity savedImage = imageRepository.save(image);
        return imageMapper.toDto(savedImage);
    }
    
    @Override
    @Transactional()
    public ImageResponse getImageById(UUID id) {
        ImageEntity image = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + id));
        return imageMapper.toResponse(image);
    }

    
    @Override
    @Transactional
    public void deleteImage(UUID id) {
        if (!imageRepository.existsById(id)) {
            throw new NotFoundException("Image not found with id: " + id);
        }
        imageRepository.deleteById(id);
    }
}