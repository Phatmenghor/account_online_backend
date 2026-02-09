package com.internal.feature.setting.controller;

import com.internal.feature.setting.dto.ImageDto;
import com.internal.feature.setting.dto.ImageResponse;
import com.internal.feature.setting.dto.ImageUploadRequest;
import com.internal.feature.setting.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    
    private final ImageService imageService;
    
    @PostMapping
    public ResponseEntity<ImageDto> uploadImage(@Valid @RequestBody ImageUploadRequest request) {
        log.info("Image upload request received");
        
        ImageDto uploadedImage = imageService.uploadImage(request);
        log.info("Image uploaded successfully with ID: {}", uploadedImage.getId());
        
        return new ResponseEntity<>(uploadedImage, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImageData(@PathVariable UUID id) {
        log.debug("Image retrieval request for ID: {}", id);
        
        ImageResponse imageResponse = imageService.getImageById(id);
        
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(imageResponse.getType()))
                .body(imageResponse.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
        log.info("Image deletion request for ID: {}", id);
        
        imageService.deleteImage(id);
        log.info("Image deleted successfully: {}", id);
        
        return ResponseEntity.noContent().build();
    }
}
