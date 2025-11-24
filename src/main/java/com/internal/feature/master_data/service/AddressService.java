package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.CommuneRequestDto;
import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.request.VillageRequestDto;
import com.internal.feature.master_data.dto.response.CommuneResponseDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.feature.master_data.dto.response.VillageResponseDto;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.utils.pagination.PaginationResponse;

import java.util.List;

public interface AddressService {
    // Province
    PaginationResponse<ProvinceResponseDto> getAllProvinces(AllMasterDataRequest request);
    ProvinceResponseDto getProvinceByCode(String code);
    ProvinceResponseDto createProvince(ProvinceRequestDto request);
    ProvinceResponseDto updateProvince(String code, ProvinceRequestDto request);
    void deleteProvince(String code);

    // District
    PaginationResponse<DistrictResponseDto> getDistrictsByProvince(AllMasterDataRequest request, String provinceCode);
    DistrictResponseDto getDistrictByCode(String code);
    DistrictResponseDto createDistrict(DistrictRequestDto request);
    DistrictResponseDto updateDistrict(String code, DistrictRequestDto request);
    void deleteDistrict(String code);

    // Commune
    PaginationResponse<CommuneResponseDto> getCommunesByDistrict(AllMasterDataRequest request, String districtCode);
    CommuneResponseDto getCommuneByCode(String code);
    CommuneResponseDto createCommune(CommuneRequestDto request);
    CommuneResponseDto updateCommune(String code, CommuneRequestDto request);
    void deleteCommune(String code);

    // Village
    PaginationResponse<VillageResponseDto> getVillagesByCommune(AllMasterDataRequest request, String communeCode);
    VillageResponseDto getVillageByCode(String code);
    VillageResponseDto createVillage(VillageRequestDto request);
    VillageResponseDto updateVillage(String code, VillageRequestDto request);
    void deleteVillage(String code);
}
