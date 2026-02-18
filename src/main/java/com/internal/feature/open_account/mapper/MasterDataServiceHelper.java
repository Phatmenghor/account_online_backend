package com.internal.feature.open_account.mapper;

import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.master_data.dto.response.LocationCodesDto;
import com.internal.feature.master_data.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MasterDataServiceHelper {

    private final MasterDataService masterDataService;

    public LocationCodesDto resolveCurrentAddress(CreateAmlRequestDto request) {
        return LocationCodesDto.builder()
                .province(masterDataService.getProvinceByCode(request.getCustomerCurrentProvince()))
                .district(masterDataService.getDistrictByCode(request.getCustomerCurrentDistrict()))
                .commune(masterDataService.getCommuneByCode(request.getCustomerCurrentCommune()))
                .village(masterDataService.getVillageByCode(request.getCustomerCurrentVillage()))
                .build();
    }

    public LocationCodesDto resolvePlaceOfBirth(CreateAmlRequestDto request) {
        return LocationCodesDto.builder()
                .province(masterDataService.getProvinceByCode(request.getCustomerPobProvince()))
                .district(masterDataService.getDistrictByCode(request.getCustomerPobDistrict()))
                .commune(masterDataService.getCommuneByCode(request.getCustomerPobCommune()))
                .village(masterDataService.getVillageByCode(request.getCustomerPobVillage()))
                .build();
    }

    public String buildFullAddressName(LocationCodesDto loc) {
        StringBuilder sb = new StringBuilder();
        if (loc.getProvince() != null)
            sb.append(loc.getProvince().getProvinceEn()).append(" (").append(loc.getProvince().getProvinceKh())
                    .append(")");
        if (loc.getDistrict() != null)
            sb.append(", ").append(loc.getDistrict().getDistrictEn()).append(" (")
                    .append(loc.getDistrict().getDistrictKh()).append(")");
        if (loc.getCommune() != null)
            sb.append(", ").append(loc.getCommune().getCommuneEn()).append(" (").append(loc.getCommune().getCommuneKh())
                    .append(")");
        if (loc.getVillage() != null)
            sb.append(", ").append(loc.getVillage().getVillageEn()).append(" (").append(loc.getVillage().getVillageKh())
                    .append(")");
        return sb.toString();
    }

    public LocationCodesDto resolveAddress(String provinceCode, String districtCode, String communeCode,
            String villageCode) {
        return LocationCodesDto.builder()
                .province(masterDataService.getProvinceByCode(provinceCode))
                .district(masterDataService.getDistrictByCode(districtCode))
                .commune(masterDataService.getCommuneByCode(communeCode))
                .village(masterDataService.getVillageByCode(villageCode))
                .build();
    }

    public String buildEnglishAddress(LocationCodesDto loc) {
        StringBuilder sb = new StringBuilder();
        // Village, Commune, District, Province
        List<String> parts = new ArrayList<>();
        if (loc.getVillage() != null)
            parts.add(loc.getVillage().getVillageEn());
        if (loc.getCommune() != null)
            parts.add(loc.getCommune().getCommuneEn());
        if (loc.getDistrict() != null)
            parts.add(loc.getDistrict().getDistrictEn());
        if (loc.getProvince() != null)
            parts.add(loc.getProvince().getProvinceEn());

        return String.join(", ", parts);
    }

    public String buildFullAddressCode(LocationCodesDto loc) {
        List<String> codes = new ArrayList<>();
        if (loc.getProvince() != null)
            codes.add(loc.getProvince().getProvinceCode());
        if (loc.getDistrict() != null)
            codes.add(loc.getDistrict().getDistrictCode());
        if (loc.getCommune() != null)
            codes.add(loc.getCommune().getCommuneCode());
        if (loc.getVillage() != null)
            codes.add(loc.getVillage().getVillageCode());
        return String.join(", ", codes);
    }
}
