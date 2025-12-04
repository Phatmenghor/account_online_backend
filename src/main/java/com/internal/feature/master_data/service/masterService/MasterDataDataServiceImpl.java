package com.internal.feature.master_data.service.masterService;

import com.internal.feature.master_data.dto.request.AddressRequestDto;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.mapper.*;
import com.internal.feature.master_data.models.*;
import com.internal.feature.master_data.repository.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.feature.master_data.specification.*;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterDataDataServiceImpl implements MasterDataService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final VillageRepository villageRepository;
    private final BranchRepository branchRepository;

    private final ProvinceMapper provinceMapper;
    private final DistrictMapper districtMapper;
    private final CommuneMapper communeMapper;
    private final VillageMapper villageMapper;
    private final BranchMapper branchMapper;

    // ---------------------- Province ----------------------
    @Override
    public PaginationResponse<ClsProvinceDto> getProvince(AllMasterDataRequest request) {
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo(), 1) - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Province> spec = ProvinceSpec.searchByName(request.getSearch());

        Page<Province> page = provinceRepository.findAll(spec, pageable);
        List<ClsProvinceDto> content = provinceMapper.toClsDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    // ---------------------- District ----------------------
    @Override
    public PaginationResponse<ClsDistrictDto> getDistrict(AllMasterDataRequest request, String provinceCode) {
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo(), 1) - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<District> spec = DistrictSpec.searchByName(request.getSearch());

        if (provinceCode != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("province").get("provinceCode"), provinceCode));
        }

        Page<District> page = districtRepository.findAll(spec, pageable);
        List<ClsDistrictDto> content = districtMapper.toClsDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    // ---------------------- Commune ----------------------
    @Override
    public PaginationResponse<ClsCommuneDto> getCommune(AllMasterDataRequest request, String districtCode) {
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo(), 1) - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Commune> spec = CommuneSpec.searchByName(request.getSearch());

        if (districtCode != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("district").get("districtCode"), districtCode));
        }

        Page<Commune> page = communeRepository.findAll(spec, pageable);
        List<ClsCommuneDto> content = communeMapper.toClsDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    // ---------------------- Village ----------------------
    @Override
    public PaginationResponse<ClsVillageDto> getVillage(AllMasterDataRequest request, String communeCode) {
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo(), 1) - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Village> spec = VillageSpec.searchByName(request.getSearch());

        if (communeCode != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("commune").get("communeCode"), communeCode));
        }

        Page<Village> page = villageRepository.findAll(spec, pageable);
        List<ClsVillageDto> content = villageMapper.toClsDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    // ---------------------- Branch ----------------------
    @Override
    public PaginationResponse<ClsBranchDto> getBranch(AllMasterDataRequest request) {
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo(), 1) - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        // Assuming Branch has a similar search spec or we create a simple one
        Specification<Branch> spec = (root, query, cb) -> {
            if (request.getSearch() == null || request.getSearch().isEmpty()) {
                return cb.conjunction();
            }
            String search = "%" + request.getSearch().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("branchCode")), search),
                    cb.like(cb.lower(root.get("branchKh")), search)
            );
        };

        Page<Branch> page = branchRepository.findAll(spec, pageable);
        List<ClsBranchDto> content = branchMapper.toClsDtoList(page.getContent());

        return new PaginationResponse<>(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    // ---------------------- Location Resolution by Names ----------------------
    @Override
    public LocationCodesDto initAddress(AddressRequestDto requestDto) {
        if (requestDto == null || requestDto.getAddress() == null || requestDto.getAddress().trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = requestDto.getAddress().trim().split("\\s+");
            if (parts.length < 1) return null;

            String villageName = null;
            String communeName = null;
            String districtName = null;
            String provinceName = null;

            int index = parts.length - 1;

            // Province is always last
            if (index >= 0) provinceName = parts[index--];

            // District prefixes
            if (index >= 0 && (parts[index].startsWith("ស្រុក") ||
                    parts[index].startsWith("ក្រុង") ||
                    parts[index].startsWith("ខណ្ឌ"))) {
                districtName = removePrefix(parts[index--], "ស្រុក", "ក្រុង", "ខណ្ឌ");
            }

            // Commune prefixes
            if (index >= 0 && (parts[index].startsWith("ឃុំ") ||
                    parts[index].startsWith("សង្កាត់"))) {
                communeName = removePrefix(parts[index--], "ឃុំ", "សង្កាត់");
            }

            // Village prefix
            if (index >= 0 && parts[index].startsWith("ភូមិ")) {
                villageName = removePrefix(parts[index--], "ភូមិ");
            }

            log.info("Resolving location - Province: {}, District: {}, Commune: {}, Village: {}",
                    provinceName, districtName, communeName, villageName);

            ClsProvinceDto province = findProvince(provinceName);
            if (province == null) return null;

            ClsDistrictDto district = districtName != null ? findDistrict(province.getProvinceCode(), districtName) : null;
            ClsCommuneDto commune = (communeName != null && district != null) ? findCommune(district.getDistrictCode(), communeName) : null;
            ClsVillageDto village = (villageName != null && commune != null) ? findVillage(commune.getCommuneCode(), villageName) : null;

            return LocationCodesDto.builder()
                    .province(province)
                    .district(district)
                    .commune(commune)
                    .village(village)
                    .build();

        } catch (Exception ex) {
            log.error("Error resolving location: {}", requestDto, ex);
            return null;
        }
    }

    // ---------------------- POB Resolution (No Village) ----------------------
    @Override
    public LocationCodesDto initPob(AddressRequestDto requestDto) {
        if (requestDto == null || requestDto.getAddress() == null || requestDto.getAddress().trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = requestDto.getAddress().trim().split("\\s+");
            if (parts.length < 1) return null;

            String communeName = null;
            String districtName = null;
            String provinceName = null;
            String villageName = null;

            int index = parts.length - 1;

            if (index >= 0) provinceName = parts[index--];

            if (index >= 0 && (parts[index].startsWith("ស្រុក") ||
                    parts[index].startsWith("ក្រុង") ||
                    parts[index].startsWith("ខណ្ឌ"))) {
                districtName = removePrefix(parts[index--], "ស្រុក", "ក្រុង", "ខណ្ឌ");
            }

            if (index >= 0 && (parts[index].startsWith("ឃុំ") ||
                    parts[index].startsWith("សង្កាត់"))) {
                communeName = removePrefix(parts[index--], "ឃុំ", "សង្កាត់");
            }

            // Village prefix
            if (index >= 0 && parts[index].startsWith("ភូមិ")) {
                villageName = removePrefix(parts[index--], "ភូមិ");
            }

            log.info("Resolving POB - Province: {}, District: {}, Commune: {}",
                    provinceName, districtName, communeName);

            ClsProvinceDto province = findProvince(provinceName);
            if (province == null) return null;

            ClsDistrictDto district = districtName != null ? findDistrict(province.getProvinceCode(), districtName) : null;
            ClsCommuneDto commune = (communeName != null && district != null) ? findCommune(district.getDistrictCode(), communeName) : null;
            ClsVillageDto village = (villageName != null && commune != null) ? findVillage(commune.getCommuneCode(), villageName) : null;

            return LocationCodesDto.builder()
                    .province(province)
                    .district(district)
                    .commune(commune)
                    .village(village)
                    .build();

        } catch (Exception ex) {
            log.error("Error resolving POB location: {}", requestDto, ex);
            return null;
        }
    }

    // ---------------------- Code-based lookups ----------------------
    @Override
    public ClsProvinceDto getProvinceByCode(String provinceCode) {
        return provinceRepository.findByProvinceCode(provinceCode)
                .map(provinceMapper::toClsDto)
                .orElse(null);
    }

    @Override
    public ClsDistrictDto getDistrictByCode(String districtCode) {
        return districtRepository.findByDistrictCode(districtCode)
                .map(districtMapper::toClsDto)
                .orElse(null);
    }

    @Override
    public ClsCommuneDto getCommuneByCode(String communeCode) {
        return communeRepository.findByCommuneCode(communeCode)
                .map(communeMapper::toClsDto)
                .orElse(null);
    }

    @Override
    public ClsVillageDto getVillageByCode(String villageCode) {
        return villageRepository.findByVillageCode(villageCode)
                .map(villageMapper::toClsDto)
                .orElse(null);
    }

    @Override
    public ClsBranchDto getBranchByCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode)
                .map(branchMapper::toClsDto)
                .orElse(null);
    }

    // ---------------------- Helper Methods ----------------------
    private String removePrefix(String text, String... prefixes) {
        if (text == null) return "";

        for (String prefix : prefixes) {
            if (text.startsWith(prefix)) {
                return text.substring(prefix.length());
            }
        }
        return text;
    }

    private ClsProvinceDto findProvince(String provinceName) {
        return provinceRepository.findFirstByProvinceKh(provinceName)
                .map(provinceMapper::toClsDto)
                .orElse(null);
    }

    private ClsDistrictDto findDistrict(String provinceCode, String districtName) {
        return districtRepository.findFirstByProvinceProvinceCodeAndDistrictKh(provinceCode, districtName)
                .map(districtMapper::toClsDto)
                .orElse(null);
    }

    private ClsCommuneDto findCommune(String districtCode, String communeName) {
        return communeRepository.findFirstByDistrictDistrictCodeAndCommuneKh(districtCode, communeName)
                .map(communeMapper::toClsDto)
                .orElse(null);
    }

    private ClsVillageDto findVillage(String communeCode, String villageName) {
        return villageRepository.findFirstByCommuneCommuneCodeAndVillageKh(communeCode, villageName)
                .map(villageMapper::toClsDto)
                .orElse(null);
    }
}
