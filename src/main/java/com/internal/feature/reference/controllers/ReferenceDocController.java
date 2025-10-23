package com.internal.feature.reference.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.reference.dto.request.*;
import com.internal.feature.reference.dto.response.AllReferenceDocResponseDto;
import com.internal.feature.reference.dto.response.ReferenceDocDto;
import com.internal.feature.reference.dto.response.ReferenceDto;
import com.internal.feature.reference.service.ReferenceDocService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reference/doc")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Doc Reference Management")
public class ReferenceDocController {
    private final ReferenceDocService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<ReferenceDocDto>> getById(@PathVariable Long id) {
        log.info("Fetching doc reference with ID: {}", id);
        ReferenceDocDto dto = service.getById(id);
        log.info("Successfully retrieved doc reference with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Doc reference retrieved successfully", dto));
    }
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllReferenceDocResponseDto>> getAll(@RequestBody GetAllReferenceDocRequest request) {
        log.info("Fetching all doc reference");
        AllReferenceDocResponseDto list = service.getAllReferenceDoc(request);
        log.info("Successfully retrieved {} doc reference", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("All doc reference retrieved successfully", list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ReferenceDocDto>> create(@RequestBody ReferenceDocCreateRequestDto request) {
        log.info("Creating new doc reference: {}", request);
        ReferenceDocDto dto = service.create(request);
        log.info("Successfully created doc reference with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success("Doc reference created successfully", dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ReferenceDocDto>> update(@PathVariable Long id,
                                                                @RequestBody ReferenceDocUpdateRequestDto request) {
        log.info("Updating doc reference with ID: {} with data: {}", id, request);
        ReferenceDocDto dto = service.update(id, request);
        log.info("Successfully updated doc reference with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Doc reference updated successfully", dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<ReferenceDocDto>> delete(@PathVariable Long id) {
        log.info("Deleting doc reference with ID: {}", id);
        ReferenceDocDto referenceDocDto = service.delete(id);
        log.info("Successfully deleted doc reference with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Doc reference deleted successfully", referenceDocDto));
    }

}
