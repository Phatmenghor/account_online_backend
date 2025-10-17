package com.internal.feature.reference.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.reference.dto.request.GetAllOccupationRequest;
import com.internal.feature.reference.dto.request.OccupationCreateRequestDto;
import com.internal.feature.reference.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllOccupationResponseDto;
import com.internal.feature.reference.dto.response.OccupationDto;
import com.internal.feature.reference.service.OccupationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/occupation")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Occupation Management")
public class OccupationController {

    private final OccupationService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<OccupationDto>> getById(@PathVariable Long id) {
        log.info("Fetching occupation with ID: {}", id);
        OccupationDto dto = service.getOccupationById(id);
        log.info("Successfully retrieved occupation with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Occupation retrieved successfully", dto));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllOccupationResponseDto>> getAll(@RequestBody GetAllOccupationRequest request) {
        log.info("Fetching all occupations");
        AllOccupationResponseDto list = service.getAllOccupations(request);
        log.info("Successfully retrieved {} occupations", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("All occupations retrieved successfully", list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OccupationDto>> create(@RequestBody OccupationCreateRequestDto request) {
        log.info("Creating new occupation: {}", request);
        OccupationDto dto = service.createOccupation(request);
        log.info("Successfully created occupation with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success("Occupation created successfully", dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<OccupationDto>> update(@PathVariable Long id,
                                                             @RequestBody OccupationUpdateRequestDto request) {
        log.info("Updating occupation with ID: {} with data: {}", id, request);
        OccupationDto dto = service.updateOccupation(id, request);
        log.info("Successfully updated occupation with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Occupation updated successfully", dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<OccupationDto>> delete(@PathVariable Long id) {
        log.info("Deleting occupation with ID: {}", id);
        OccupationDto occupationDto = service.deleteOccupation(id);
        log.info("Successfully deleted occupation with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Occupation deleted successfully", occupationDto));
    }
}
