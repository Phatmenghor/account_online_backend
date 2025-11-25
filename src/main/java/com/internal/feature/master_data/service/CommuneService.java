package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.CommuneRequestDto;
import com.internal.feature.master_data.dto.response.CommuneResponseDto;
import com.internal.utils.pagination.PaginationResponse;

public interface CommuneService {
    PaginationResponse<CommuneResponseDto> getAllCommunes(AllMasterDataRequest request);
    PaginationResponse<CommuneResponseDto> getCommunesByDistrict(AllMasterDataRequest request, String districtCode);
    CommuneResponseDto getCommuneById(Long id);
    CommuneResponseDto createCommune(CommuneRequestDto request);
    CommuneResponseDto updateCommune(Long id, CommuneRequestDto request);
    void deleteCommune(Long id);
}
