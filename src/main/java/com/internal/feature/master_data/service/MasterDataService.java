package com.internal.feature.master_data.service;

import com.internal.feature.master_data.dto.request.AddressRequestDto;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.response.*;
import com.internal.utils.pagination.PaginationResponse;

public interface MasterDataService {

    PaginationResponse<ClsProvinceDto> getProvince(AllMasterDataRequest request);

    PaginationResponse<ClsDistrictDto> getDistrict(AllMasterDataRequest request, String provinceCode);

    PaginationResponse<ClsCommuneDto> getCommune(AllMasterDataRequest request, String districtCode);

    PaginationResponse<ClsVillageDto> getVillage(AllMasterDataRequest request, String communeCode);

    PaginationResponse<ClsBranchDto> getBranch(AllMasterDataRequest request);

    // ---------------------- Location Resolution by Names ----------------------
    LocationCodesDto initAddress(AddressRequestDto fullLocationString);

    // ---------------------- POB Resolution (No Village) ----------------------
    LocationCodesDto initPob(AddressRequestDto pobString);

    // ---------------------- Code-based lookups ----------------------
    // USED BY OTHER SERVICES (AML, Open Account, Logs Report) - DO NOT MODIFY WITHOUT CHECKING DEPENDENCIES
    ClsProvinceDto getProvinceByCode(String provinceCode);

    // USED BY OTHER SERVICES (AML, Open Account, Logs Report) - DO NOT MODIFY WITHOUT CHECKING DEPENDENCIES
    ClsDistrictDto getDistrictByCode(String districtCode);

    // USED BY OTHER SERVICES (AML, Open Account, Logs Report) - DO NOT MODIFY WITHOUT CHECKING DEPENDENCIES
    ClsCommuneDto getCommuneByCode(String communeCode);

    // USED BY OTHER SERVICES (AML, Open Account, Logs Report) - DO NOT MODIFY WITHOUT CHECKING DEPENDENCIES
    ClsVillageDto getVillageByCode(String villageCode);

    // USED BY OTHER SERVICES (AML, Open Account, Logs Report) - DO NOT MODIFY WITHOUT CHECKING DEPENDENCIES
    ClsBranchDto getBranchByCode(String branchCode);
}
