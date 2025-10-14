package com.internal.feature.master_data.service.masterService;

import com.internal.feature.master_data.dto.response.*;
import com.internal.utils.pagination.PaginationResponse;

public interface MasterDataService {

    PaginationResponse<ClsProvinceDto> getProvince(int pageNo, int pageSize);

    PaginationResponse<ClsDistrictDto> getDistrict(String provinceCode, int pageNo, int pageSize);

    PaginationResponse<ClsCommuneDto> getCommune(String districtCode, int pageNo, int pageSize);

    PaginationResponse<ClsVillageDto> getVillage(String communeCode, int pageNo, int pageSize);

}
