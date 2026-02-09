package com.internal.feature.master_data.controller;

import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.CommuneRequestDto;
import com.internal.feature.master_data.dto.response.CommuneResponseDto;
import com.internal.feature.master_data.service.CommuneService;
import com.internal.utils.constants.ResponseMessage;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/master-data/address")
@RequiredArgsConstructor
@Slf4j
public class CommuneController {

    private final CommuneService communeService;

    @PostMapping("/communes/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CommuneResponseDto>>> getAllCommunes(@RequestBody AllMasterDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.ALL_COMMUNES), communeService.getAllCommunes(request)));
    }

    @PostMapping("/communes/get-by-id/{id}")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> getCommuneById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.retrieved(ResponseMessage.COMMUNE), communeService.getCommuneById(id)));
    }

    @PostMapping("/communes/create")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> createCommune(@RequestBody CommuneRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.created(ResponseMessage.COMMUNE), communeService.createCommune(request)));
    }

    @PostMapping("/communes/update/{id}")
    public ResponseEntity<ApiResponse<CommuneResponseDto>> updateCommune(@PathVariable Long id, @RequestBody CommuneRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.updated(ResponseMessage.COMMUNE), communeService.updateCommune(id, request)));
    }

    @PostMapping("/communes/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCommune(@PathVariable Long id) {
        communeService.deleteCommune(id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.deleted(ResponseMessage.COMMUNE), null));
    }
}
