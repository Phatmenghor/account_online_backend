package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.VillageRequestDto;
import com.internal.feature.master_data.dto.response.VillageResponseDto;
import com.internal.utils.pagination.PaginationResponse;

public interface VillageService {
    PaginationResponse<VillageResponseDto> getAllVillages(AllMasterDataRequest request);
    PaginationResponse<VillageResponseDto> getVillagesByCommune(AllMasterDataRequest request, String communeCode);
    VillageResponseDto getVillageById(Long id);
    VillageResponseDto createVillage(VillageRequestDto request);
    VillageResponseDto updateVillage(Long id, VillageRequestDto request);
    void deleteVillage(Long id);
}
