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
    ProvinceResponseDto getProvinceById(Long id);
    ProvinceResponseDto createProvince(ProvinceRequestDto request);
    ProvinceResponseDto updateProvince(Long id, ProvinceRequestDto request);
    void deleteProvince(Long id);

    // District
    PaginationResponse<DistrictResponseDto> getAllDistricts(AllMasterDataRequest request);
    PaginationResponse<DistrictResponseDto> getDistrictsByProvince(AllMasterDataRequest request, String provinceCode);
    DistrictResponseDto getDistrictById(Long id);
    DistrictResponseDto createDistrict(DistrictRequestDto request);
    DistrictResponseDto updateDistrict(Long id, DistrictRequestDto request);
    void deleteDistrict(Long id);

    // Commune
    PaginationResponse<CommuneResponseDto> getAllCommunes(AllMasterDataRequest request);
    PaginationResponse<CommuneResponseDto> getCommunesByDistrict(AllMasterDataRequest request, String districtCode);
    CommuneResponseDto getCommuneById(Long id);
    CommuneResponseDto createCommune(CommuneRequestDto request);
    CommuneResponseDto updateCommune(Long id, CommuneRequestDto request);
    void deleteCommune(Long id);

    // Village
    PaginationResponse<VillageResponseDto> getAllVillages(AllMasterDataRequest request);
    PaginationResponse<VillageResponseDto> getVillagesByCommune(AllMasterDataRequest request, String communeCode);
    VillageResponseDto getVillageById(Long id);
    VillageResponseDto createVillage(VillageRequestDto request);
    VillageResponseDto updateVillage(Long id, VillageRequestDto request);
    void deleteVillage(Long id);
}
