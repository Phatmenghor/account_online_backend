package com.internal.feature.setting.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.setting.dto.ImageDto;
import com.internal.feature.setting.dto.ImageResponse;
import com.internal.feature.setting.dto.ImageUploadRequest;
import com.internal.feature.setting.mapper.ImageMapper;
import com.internal.feature.setting.models.ImageEntity;
import com.internal.feature.setting.repository.ImageRepository;
import com.internal.feature.setting.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @Override
    @Transactional
    public ImageDto uploadImage(ImageUploadRequest request) {
        log.debug("Processing image upload");
        
        normalizeImageType(request);
        
        ImageEntity image = imageMapper.toEntity(request);
        ImageEntity savedImage = imageRepository.save(image);
        
        log.info("Image uploaded and saved with ID: {}", savedImage.getId());
        return imageMapper.toDto(savedImage);
    }
    
    @Override
    public ImageResponse getImageById(UUID id) {
        log.debug("Retrieving image with ID: {}", id);
        
        ImageEntity image = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + id));
                
        return imageMapper.toResponse(image);
    }
    
    @Override
    @Transactional
    public void deleteImage(UUID id) {
        log.debug("Deleting image with ID: {}", id);
        
        if (!imageRepository.existsById(id)) {
            throw new NotFoundException("Image not found with id: " + id);
        }
        
        imageRepository.deleteById(id);
        log.info("Image deleted: {}", id);
    }

    private void normalizeImageType(ImageUploadRequest request) {
        if (!request.getType().contains("/")) {
            request.setType("image/" + request.getType());
        }
    }
}
