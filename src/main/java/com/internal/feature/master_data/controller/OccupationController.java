package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.GetAllOccupationRequest;
import com.internal.feature.master_data.dto.request.OccupationCreateRequestDto;
import com.internal.feature.master_data.dto.request.OccupationUpdateRequestDto;
import com.internal.feature.master_data.dto.response.AllOccupationResponseDto;
import com.internal.feature.master_data.dto.response.OccupationDto;
import com.internal.feature.master_data.service.OccupationService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/occupation")
@RequiredArgsConstructor
@Slf4j
public class OccupationController {

    private final OccupationService service;

    @PostMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<OccupationDto>> getById(@PathVariable Long id) {
        log.info("Fetching occupation with ID: {}", id);
        OccupationDto dto = service.getOccupationById(id);
        log.info("Successfully retrieved occupation with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.OCCUPATION), dto));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<AllOccupationResponseDto>> getAll(@RequestBody GetAllOccupationRequest request) {
        log.info("Fetching all occupations");
        AllOccupationResponseDto list = service.getAllOccupations(request);
        log.info("Successfully retrieved {} occupations", list.getContent().size());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_OCCUPATIONS), list));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OccupationDto>> create(@RequestBody OccupationCreateRequestDto request) {
        log.info("Creating new occupation: {}", request);
        OccupationDto dto = service.createOccupation(request);
        log.info("Successfully created occupation with ID: {}", dto.getId());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.OCCUPATION), dto));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<OccupationDto>> update(@PathVariable Long id,
                                                             @RequestBody OccupationUpdateRequestDto request) {
        log.info("Updating occupation with ID: {} with data: {}", id, request);
        OccupationDto dto = service.updateOccupation(id, request);
        log.info("Successfully updated occupation with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.OCCUPATION), dto));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<OccupationDto>> delete(@PathVariable Long id) {
        log.info("Deleting occupation with ID: {}", id);
        OccupationDto occupationDto = service.deleteOccupation(id);
        log.info("Successfully deleted occupation with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.OCCUPATION), occupationDto));
    }
}
