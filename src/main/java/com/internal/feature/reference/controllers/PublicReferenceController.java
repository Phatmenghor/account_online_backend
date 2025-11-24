package com.internal.feature.reference.controllers;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.reference.dto.request.PublicReferenceRequest;
import com.internal.feature.reference.dto.response.MaritalStatusDto;
import com.internal.feature.reference.dto.response.OccupationDto;
import com.internal.feature.reference.dto.response.ReferenceDocDto;
import com.internal.feature.reference.dto.response.ReferenceDto;
import com.internal.feature.reference.service.MaritalStatusService;
import com.internal.feature.reference.service.OccupationService;
import com.internal.feature.reference.service.ReferenceDocService;
import com.internal.feature.reference.service.ReferenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/master-data")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Master Data Management (public)")
public class PublicReferenceController {

    private final OccupationService occupationService;
    private final MaritalStatusService maritalStatusService;
    private final ReferenceService referenceService;
    private final ReferenceDocService referenceDocService;

    @PostMapping("/occupation/all")
    public ResponseEntity<ApiResponse<List<OccupationDto>>> getAllOccupations(@RequestBody PublicReferenceRequest request) {
        log.info("Fetching all occupations (public)");
        List<OccupationDto> list = occupationService.getAllOccupationsPublic(request.getSearch());
        log.info("Successfully retrieved {} occupations", list.size());
        return ResponseEntity.ok(ApiResponse.success("All occupations retrieved successfully", list));
    }

    @PostMapping("/marital-status/all")
    public ResponseEntity<ApiResponse<List<MaritalStatusDto>>> getAllMaritalStatus(@RequestBody PublicReferenceRequest request) {
        log.info("Fetching all marital statuses (public)");
        List<MaritalStatusDto> list = maritalStatusService.getAllPublic(request.getSearch());
        log.info("Successfully retrieved {} marital statuses", list.size());
        return ResponseEntity.ok(ApiResponse.success("All marital statuses retrieved successfully", list));
    }

    @PostMapping("/bank/all")
    public ResponseEntity<ApiResponse<List<ReferenceDto>>> getAllReferences(@RequestBody PublicReferenceRequest request) {
        log.info("Fetching all banks (public)");
        List<ReferenceDto> list = referenceService.getAllPublic(request.getSearch());
        log.info("Successfully retrieved {} banks", list.size());
        return ResponseEntity.ok(ApiResponse.success("All banks retrieved successfully", list));
    }

    @PostMapping("/doc/all")
    public ResponseEntity<ApiResponse<List<ReferenceDocDto>>> getAllReferenceDocs(@RequestBody PublicReferenceRequest request) {
        log.info("Fetching all doc references (public)");
        List<ReferenceDocDto> list = referenceDocService.getAllReferenceDocPublic(request.getSearch());
        log.info("Successfully retrieved {} doc references", list.size());
        return ResponseEntity.ok(ApiResponse.success("All doc references retrieved successfully", list));
    }
}
