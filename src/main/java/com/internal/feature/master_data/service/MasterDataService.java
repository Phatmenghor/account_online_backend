package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.response.*;
import com.internal.utils.pagination.PaginationResponse;

public interface MasterDataService {

    PaginationResponse<ClsProvinceDto> getProvince(AllMasterDataRequest request);

    PaginationResponse<ClsDistrictDto> getDistrict(AllMasterDataRequest request, String provinceCode);

    PaginationResponse<ClsCommuneDto> getCommune(AllMasterDataRequest request, String districtCode);

    PaginationResponse<ClsVillageDto> getVillage(AllMasterDataRequest request, String communeCode);

    PaginationResponse<ClsBranchDto> getBranch(AllMasterDataRequest request);
}
