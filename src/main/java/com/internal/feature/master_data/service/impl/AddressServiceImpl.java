package com.internal.feature.master_data.service.impl;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.master_data.dto.request.CommuneRequestDto;
import com.internal.feature.master_data.dto.request.DistrictRequestDto;
import com.internal.feature.master_data.dto.request.ProvinceRequestDto;
import com.internal.feature.master_data.dto.request.VillageRequestDto;
import com.internal.feature.master_data.dto.response.CommuneResponseDto;
import com.internal.feature.master_data.dto.response.DistrictResponseDto;
import com.internal.feature.master_data.dto.response.ProvinceResponseDto;
import com.internal.feature.master_data.dto.response.VillageResponseDto;
import com.internal.feature.master_data.models.Commune;
import com.internal.feature.master_data.models.District;
import com.internal.feature.master_data.models.Province;
import com.internal.feature.master_data.models.Village;
import com.internal.feature.master_data.repository.CommuneRepository;
import com.internal.feature.master_data.repository.DistrictRepository;
import com.internal.feature.master_data.repository.ProvinceRepository;
import com.internal.feature.master_data.repository.VillageRepository;
import com.internal.feature.master_data.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.specification.CommuneSpec;
import com.internal.feature.master_data.specification.DistrictSpec;
import com.internal.feature.master_data.specification.ProvinceSpec;
import com.internal.feature.master_data.specification.VillageSpec;
import com.internal.utils.pagination.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final VillageRepository villageRepository;

    // Province
    @Override
    public PaginationResponse<ProvinceResponseDto> getAllProvinces(AllMasterDataRequest request) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<Province> spec = ProvinceSpec.searchByName(request.getSearch());

        Page<Province> page = provinceRepository.findAll(spec, pageable);
        List<ProvinceResponseDto> content = page.stream()
                .map(this::mapToProvinceDto)
                .collect(Collectors.toList());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public ProvinceResponseDto getProvinceByCode(String code) {
        Province province = provinceRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Province not found with code: " + code));
        return mapToProvinceDto(province);
    }

    @Override
    @Transactional
    public ProvinceResponseDto createProvince(ProvinceRequestDto request) {
        if (provinceRepository.existsById(request.getProvinceCode())) {
            throw new RuntimeException("Province with code " + request.getProvinceCode() + " already exists");
        }
        Province province = new Province();
        province.setProvinceCode(request.getProvinceCode());
        province.setProvinceEn(request.getProvinceEn());
        province.setProvinceKh(request.getProvinceKh());
        return mapToProvinceDto(provinceRepository.save(province));
    }

    @Override
    @Transactional
    public ProvinceResponseDto updateProvince(String code, ProvinceRequestDto request) {
        Province province = provinceRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Province not found with code: " + code));
        province.setProvinceEn(request.getProvinceEn());
        province.setProvinceKh(request.getProvinceKh());
        return mapToProvinceDto(provinceRepository.save(province));
    }

    @Override
    @Transactional
    public void deleteProvince(String code) {
        if (!provinceRepository.existsById(code)) {
            throw new NotFoundException("Province not found with code: " + code);
        }
        provinceRepository.deleteById(code);
    }

    // District
    @Override
    public PaginationResponse<DistrictResponseDto> getDistrictsByProvince(AllMasterDataRequest request, String provinceCode) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<District> spec = DistrictSpec.searchByName(request.getSearch())
                .and((root, query, cb) -> cb.equal(root.get("province").get("provinceCode"), provinceCode));

        Page<District> page = districtRepository.findAll(spec, pageable);
        List<DistrictResponseDto> content = page.stream()
                .map(this::mapToDistrictDto)
                .collect(Collectors.toList());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public DistrictResponseDto getDistrictByCode(String code) {
        District district = districtRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("District not found with code: " + code));
        return mapToDistrictDto(district);
    }

    @Override
    @Transactional
    public DistrictResponseDto createDistrict(DistrictRequestDto request) {
        if (districtRepository.existsById(request.getDistrictCode())) {
            throw new RuntimeException("District with code " + request.getDistrictCode() + " already exists");
        }
        Province province = provinceRepository.findById(request.getProvinceCode())
                .orElseThrow(() -> new NotFoundException("Province not found with code: " + request.getProvinceCode()));
        
        District district = new District();
        district.setDistrictCode(request.getDistrictCode());
        district.setDistrictEn(request.getDistrictEn());
        district.setDistrictKh(request.getDistrictKh());
        district.setProvince(province);
        return mapToDistrictDto(districtRepository.save(district));
    }

    @Override
    @Transactional
    public DistrictResponseDto updateDistrict(String code, DistrictRequestDto request) {
        District district = districtRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("District not found with code: " + code));
        
        if (!district.getProvince().getProvinceCode().equals(request.getProvinceCode())) {
             Province province = provinceRepository.findById(request.getProvinceCode())
                .orElseThrow(() -> new NotFoundException("Province not found with code: " + request.getProvinceCode()));
             district.setProvince(province);
        }

        district.setDistrictEn(request.getDistrictEn());
        district.setDistrictKh(request.getDistrictKh());
        return mapToDistrictDto(districtRepository.save(district));
    }

    @Override
    @Transactional
    public void deleteDistrict(String code) {
        if (!districtRepository.existsById(code)) {
            throw new NotFoundException("District not found with code: " + code);
        }
        districtRepository.deleteById(code);
    }

    // Commune
    @Override
    public PaginationResponse<CommuneResponseDto> getCommunesByDistrict(AllMasterDataRequest request, String districtCode) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<Commune> spec = CommuneSpec.searchByName(request.getSearch())
                .and((root, query, cb) -> cb.equal(root.get("district").get("districtCode"), districtCode));

        Page<Commune> page = communeRepository.findAll(spec, pageable);
        List<CommuneResponseDto> content = page.stream()
                .map(this::mapToCommuneDto)
                .collect(Collectors.toList());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public CommuneResponseDto getCommuneByCode(String code) {
        Commune commune = communeRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Commune not found with code: " + code));
        return mapToCommuneDto(commune);
    }

    @Override
    @Transactional
    public CommuneResponseDto createCommune(CommuneRequestDto request) {
        if (communeRepository.existsById(request.getCommuneCode())) {
            throw new RuntimeException("Commune with code " + request.getCommuneCode() + " already exists");
        }
        District district = districtRepository.findById(request.getDistrictCode())
                .orElseThrow(() -> new NotFoundException("District not found with code: " + request.getDistrictCode()));

        Commune commune = new Commune();
        commune.setCommuneCode(request.getCommuneCode());
        commune.setCommuneEn(request.getCommuneEn());
        commune.setCommuneKh(request.getCommuneKh());
        commune.setDistrict(district);
        return mapToCommuneDto(communeRepository.save(commune));
    }

    @Override
    @Transactional
    public CommuneResponseDto updateCommune(String code, CommuneRequestDto request) {
        Commune commune = communeRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Commune not found with code: " + code));

        if (!commune.getDistrict().getDistrictCode().equals(request.getDistrictCode())) {
            District district = districtRepository.findById(request.getDistrictCode())
                    .orElseThrow(() -> new NotFoundException("District not found with code: " + request.getDistrictCode()));
            commune.setDistrict(district);
        }

        commune.setCommuneEn(request.getCommuneEn());
        commune.setCommuneKh(request.getCommuneKh());
        return mapToCommuneDto(communeRepository.save(commune));
    }

    @Override
    @Transactional
    public void deleteCommune(String code) {
        if (!communeRepository.existsById(code)) {
            throw new NotFoundException("Commune not found with code: " + code);
        }
        communeRepository.deleteById(code);
    }

    // Village
    @Override
    public PaginationResponse<VillageResponseDto> getVillagesByCommune(AllMasterDataRequest request, String communeCode) {
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        Specification<Village> spec = VillageSpec.searchByName(request.getSearch())
                .and((root, query, cb) -> cb.equal(root.get("commune").get("communeCode"), communeCode));

        Page<Village> page = villageRepository.findAll(spec, pageable);
        List<VillageResponseDto> content = page.stream()
                .map(this::mapToVillageDto)
                .collect(Collectors.toList());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    public VillageResponseDto getVillageByCode(String code) {
        Village village = villageRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Village not found with code: " + code));
        return mapToVillageDto(village);
    }

    @Override
    @Transactional
    public VillageResponseDto createVillage(VillageRequestDto request) {
        if (villageRepository.existsById(request.getVillageCode())) {
            throw new RuntimeException("Village with code " + request.getVillageCode() + " already exists");
        }
        Commune commune = communeRepository.findById(request.getCommuneCode())
                .orElseThrow(() -> new NotFoundException("Commune not found with code: " + request.getCommuneCode()));

        Village village = new Village();
        village.setVillageCode(request.getVillageCode());
        village.setVillageEn(request.getVillageEn());
        village.setVillageKh(request.getVillageKh());
        village.setCommune(commune);
        return mapToVillageDto(villageRepository.save(village));
    }

    @Override
    @Transactional
    public VillageResponseDto updateVillage(String code, VillageRequestDto request) {
        Village village = villageRepository.findById(code)
                .orElseThrow(() -> new NotFoundException("Village not found with code: " + code));

        if (!village.getCommune().getCommuneCode().equals(request.getCommuneCode())) {
            Commune commune = communeRepository.findById(request.getCommuneCode())
                    .orElseThrow(() -> new NotFoundException("Commune not found with code: " + request.getCommuneCode()));
            village.setCommune(commune);
        }

        village.setVillageEn(request.getVillageEn());
        village.setVillageKh(request.getVillageKh());
        return mapToVillageDto(villageRepository.save(village));
    }

    @Override
    @Transactional
    public void deleteVillage(String code) {
        if (!villageRepository.existsById(code)) {
            throw new NotFoundException("Village not found with code: " + code);
        }
        villageRepository.deleteById(code);
    }

    // Mappers
    private ProvinceResponseDto mapToProvinceDto(Province province) {
        return ProvinceResponseDto.builder()
                .provinceCode(province.getProvinceCode())
                .provinceEn(province.getProvinceEn())
                .provinceKh(province.getProvinceKh())
                .build();
    }

    private DistrictResponseDto mapToDistrictDto(District district) {
        return DistrictResponseDto.builder()
                .districtCode(district.getDistrictCode())
                .districtEn(district.getDistrictEn())
                .districtKh(district.getDistrictKh())
                .province(mapToProvinceDto(district.getProvince()))
                .build();
    }

    private CommuneResponseDto mapToCommuneDto(Commune commune) {
        return CommuneResponseDto.builder()
                .communeCode(commune.getCommuneCode())
                .communeEn(commune.getCommuneEn())
                .communeKh(commune.getCommuneKh())
                .district(mapToDistrictDto(commune.getDistrict()))
                .build();
    }

    private VillageResponseDto mapToVillageDto(Village village) {
        return VillageResponseDto.builder()
                .villageCode(village.getVillageCode())
                .villageEn(village.getVillageEn())
                .villageKh(village.getVillageKh())
                .commune(mapToCommuneDto(village.getCommune()))
                .build();
    }
}
