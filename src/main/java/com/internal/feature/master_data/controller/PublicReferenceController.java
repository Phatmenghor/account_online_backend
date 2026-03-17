package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.PublicReferenceRequest;
import com.internal.feature.master_data.dto.response.MaritalStatusDto;
import com.internal.feature.master_data.dto.response.OccupationDto;
import com.internal.feature.master_data.dto.response.LegalTypeDto;
import com.internal.feature.master_data.dto.response.ReferenceDto;
import com.internal.feature.master_data.service.MaritalStatusService;
import com.internal.feature.master_data.service.OccupationService;
import com.internal.feature.master_data.service.LegalTypeService;
import com.internal.feature.master_data.service.ReferenceService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/master-data")
@RequiredArgsConstructor
@Slf4j
public class PublicReferenceController {

    private final OccupationService occupationService;
    private final MaritalStatusService maritalStatusService;
    private final ReferenceService referenceService;
    private final LegalTypeService legalTypeService;

    @PostMapping("/occupation/all")
    public ResponseEntity<ApiResponse<List<OccupationDto>>> getAllOccupations(@RequestBody PublicReferenceRequest request) {
        long startTime = System.currentTimeMillis();
        log.info(">>> FETCHING OCCUPATIONS");
        List<OccupationDto> list = occupationService.getAllOccupationsPublic(request.getSearch());
        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ SUCCESS: Retrieved {} occupations ({}ms)", list.size(), duration);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_OCCUPATIONS), list));
    }

    @PostMapping("/marital-status/all")
    public ResponseEntity<ApiResponse<List<MaritalStatusDto>>> getAllMaritalStatus(@RequestBody PublicReferenceRequest request) {
        long startTime = System.currentTimeMillis();
        log.info(">>> FETCHING MARITAL STATUSES");
        List<MaritalStatusDto> list = maritalStatusService.getAllPublic(request.getSearch());
        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ SUCCESS: Retrieved {} marital statuses ({}ms)", list.size(), duration);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_MARITAL_STATUSES), list));
    }

    @PostMapping("/bank/all")
    public ResponseEntity<ApiResponse<List<ReferenceDto>>> getAllReferences(@RequestBody PublicReferenceRequest request) {
        long startTime = System.currentTimeMillis();
        log.info(">>> FETCHING BANKS");
        List<ReferenceDto> list = referenceService.getAllPublic(request.getSearch());
        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ SUCCESS: Retrieved {} banks ({}ms)", list.size(), duration);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_BANKS), list));
    }

    @PostMapping("/legal-type/all")
    public ResponseEntity<ApiResponse<List<LegalTypeDto>>> getAllLegalTypes(@RequestBody PublicReferenceRequest request) {
        long startTime = System.currentTimeMillis();
        log.info(">>> FETCHING LEGAL TYPES");
        List<LegalTypeDto> list = legalTypeService.getAllLegalTypePublic(request.getSearch());
        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ SUCCESS: Retrieved {} legal types ({}ms)", list.size(), duration);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_LEGAL_TYPES), list));
    }
}
