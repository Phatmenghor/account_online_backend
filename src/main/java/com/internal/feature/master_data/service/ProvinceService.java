package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.utils.pagination.PaginationResponse;

public interface ProvinceService {
    PaginationResponse<ProvinceResponseDto> getAllProvinces(AllMasterDataRequest request);
    ProvinceResponseDto getProvinceById(Long id);
    ProvinceResponseDto createProvince(ProvinceRequestDto request);
    ProvinceResponseDto updateProvince(Long id, ProvinceRequestDto request);
    void deleteProvince(Long id);
}
