package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.utils.pagination.PaginationResponse;

public interface DistrictService {
    PaginationResponse<DistrictResponseDto> getAllDistricts(AllMasterDataRequest request);
    PaginationResponse<DistrictResponseDto> getDistrictsByProvince(AllMasterDataRequest request, String provinceCode);
    DistrictResponseDto getDistrictById(Long id);
    DistrictResponseDto createDistrict(DistrictRequestDto request);
    DistrictResponseDto updateDistrict(Long id, DistrictRequestDto request);
    void deleteDistrict(Long id);
}
