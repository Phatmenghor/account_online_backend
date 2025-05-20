package com.account_sell.feature.setting.controller;

import com.account_sell.feature.setting.dto.ImageDto;
import com.account_sell.feature.setting.dto.ImageResponse;
import com.account_sell.feature.setting.dto.ImageUploadRequest;
import com.account_sell.feature.setting.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    
    private final ImageService imageService;
    
    @PostMapping
    public ResponseEntity<ImageDto> uploadImage(@Valid @RequestBody ImageUploadRequest request) {
        ImageDto uploadedImage = imageService.uploadImage(request);
        return new ResponseEntity<>(uploadedImage, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImageData(@PathVariable UUID id) {
        ImageResponse imageResponse = imageService.getImageById(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(imageResponse.getType()))
                .body(imageResponse.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}