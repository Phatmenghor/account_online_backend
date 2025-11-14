package com.internal.feature.master_data.service.masterService;

import com.internal.exceptions.error.MasterDataServiceException;
import com.internal.feature.master_data.dto.request.AddressRequestDto;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.request.LocationFilter;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.utils.constants.HelperUtils;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterDataDataServiceImpl implements MasterDataService {

    private final HelperUtils helperUtils;

    @Qualifier("postgresJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    // ---------------------- Province ----------------------
    @Override
    public PaginationResponse<ClsProvinceDto> getProvince(AllMasterDataRequest request) {
        return getPaginatedData(
                jdbcTemplate,
                request,
                "acc_online_province_cbc",
                new ProvinceRowMapper(),
                "province_code",
                "province_en",
                "province_kh",
                null // no parent filter
        );
    }

    // ---------------------- District ----------------------
    @Override
    public PaginationResponse<ClsDistrictDto> getDistrict(AllMasterDataRequest request, String provinceCode) {
        LocationFilter filter = LocationFilter.builder().provinceCode(provinceCode).build();
        return getPaginatedData(
                jdbcTemplate,
                request,
                "acc_online_district_cbc",
                new DistrictRowMapper(),
                "district_code",
                "district_en",
                "district_kh",
                filter
        );
    }

    // ---------------------- Commune ----------------------
    @Override
    public PaginationResponse<ClsCommuneDto> getCommune(AllMasterDataRequest request, String districtCode) {
        LocationFilter filter = LocationFilter.builder().districtCode(districtCode).build();
        return getPaginatedData(
                jdbcTemplate,
                request,
                "acc_online_commune_cbc",
                new CommuneRowMapper(),
                "commune_code",
                "commune_en",
                "commune_kh",
                filter
        );
    }

    // ---------------------- Village ----------------------
    @Override
    public PaginationResponse<ClsVillageDto> getVillage(AllMasterDataRequest request, String communeCode) {
        LocationFilter filter = LocationFilter.builder().communeCode(communeCode).build();
        return getPaginatedData(
                jdbcTemplate,
                request,
                "acc_online_village_cbc",
                new VillageRowMapper(),
                "village_code",
                "village_en",
                "village_kh",
                filter
        );
    }

    // ---------------------- Branch ----------------------
    @Override
    public PaginationResponse<ClsBranchDto> getBranch(AllMasterDataRequest request) {
        return getPaginatedData(
                jdbcTemplate,
                request,
                "acc_online_branch",
                new BranchRowMapper(),
                "branch_code",
                null,
                "branch_kh",
                null
        );
    }

    // ---------------------- Location Resolution by Names ----------------------
    @Override
    public LocationCodesDto initAddress(AddressRequestDto requestDto) {
        if (requestDto == null || requestDto.getAddress().trim().isEmpty()) {
            return null;
        }

        try {
            // Parse the location string
            String[] parts = requestDto.getAddress().trim().split("\\s+");

            if (parts.length < 1) {
                log.warn("Invalid location format: {}", requestDto);
                return null;
            }

            // Initialize variables
            String villageName = null;
            String communeName = null;
            String districtName = null;
            String provinceName = null;

            // Parse backwards from the end (province is always last)
            int index = parts.length - 1;

            // Last part is always province (no prefix)
            if (index >= 0) {
                provinceName = parts[index--];
            }

            // Look for district (ស្រុក, ក្រុង, ខណ្ឌ)
            if (index >= 0 && (parts[index].startsWith("ស្រុក") ||
                    parts[index].startsWith("ក្រុង") ||
                    parts[index].startsWith("ខណ្ឌ"))) {
                districtName = removePrefix(parts[index--], "ស្រុក", "ក្រុង", "ខណ្ឌ");
            }

            // Look for commune (ឃុំ, សង្កាត់)
            if (index >= 0 && (parts[index].startsWith("ឃុំ") ||
                    parts[index].startsWith("សង្កាត់"))) {
                communeName = removePrefix(parts[index--], "ឃុំ", "សង្កាត់");
            }

            // Look for village (ភូមិ)
            if (index >= 0 && parts[index].startsWith("ភូមិ")) {
                villageName = removePrefix(parts[index--], "ភូមិ");
            }

            log.info("Resolving location - Province: {}, District: {}, Commune: {}, Village: {}",
                    provinceName, districtName, communeName, villageName);

            // Step 1: Find Province (required)
            if (provinceName == null) {
                log.warn("Province not found in address: {}", requestDto);
                return null;
            }
            ClsProvinceDto province = findProvince(provinceName);
            if (province == null) {
                log.warn("Province not found: {}", provinceName);
                return null;
            }

            // Step 2: Find District (optional)
            ClsDistrictDto district = null;
            if (districtName != null) {
                district = findDistrict(province.getProvinceCode(), districtName);
                if (district == null) {
                    log.warn("District not found: {} in province {}", districtName, provinceName);
                    // Don't return null, continue with what we have
                }
            }

            // Step 3: Find Commune (optional, requires district)
            ClsCommuneDto commune = null;
            if (communeName != null && district != null) {
                commune = findCommune(district.getDistrictCode(), communeName);
                if (commune == null) {
                    log.warn("Commune not found: {} in district {}", communeName, districtName);
                    // Don't return null, continue with what we have
                }
            }

            // Step 4: Find Village (optional, requires commune)
            ClsVillageDto village = null;
            if (villageName != null && commune != null) {
                village = findVillage(commune.getCommuneCode(), villageName);
                if (village == null) {
                    log.warn("Village not found: {} in commune {}", villageName, communeName);
                    // Don't return null, continue with what we have
                }
            }

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
        if (requestDto == null || requestDto.getAddress().trim().isEmpty()) {
            return null;
        }

        try {
            // Parse the POB string
            String[] parts = requestDto.getAddress().trim().split("\\s+");

            if (parts.length < 1) {
                log.warn("Invalid POB format: {}", requestDto);
                return null;
            }

            // Initialize variables
            String communeName = null;
            String districtName = null;
            String provinceName = null;

            // Parse backwards from the end (province is always last)
            int index = parts.length - 1;

            // Last part is always province (no prefix)
            if (index >= 0) {
                provinceName = parts[index--];
            }

            // Look for district (ស្រុក, ក្រុង, ខណ្ឌ)
            if (index >= 0 && (parts[index].startsWith("ស្រុក") ||
                    parts[index].startsWith("ក្រុង") ||
                    parts[index].startsWith("ខណ្ឌ"))) {
                districtName = removePrefix(parts[index--], "ស្រុក", "ក្រុង", "ខណ្ឌ");
            }

            // Look for commune (ឃុំ, សង្កាត់)
            if (index >= 0 && (parts[index].startsWith("ឃុំ") ||
                    parts[index].startsWith("សង្កាត់"))) {
                communeName = removePrefix(parts[index--], "ឃុំ", "សង្កាត់");
            }

            log.info("Resolving POB - Province: {}, District: {}, Commune: {}",
                    provinceName, districtName, communeName);

            // Step 1: Find Province (required)
            if (provinceName == null) {
                log.warn("Province not found in POB: {}", requestDto);
                return null;
            }
            ClsProvinceDto province = findProvince(provinceName);
            if (province == null) {
                log.warn("Province not found: {}", provinceName);
                return null;
            }

            // Step 2: Find District (optional)
            ClsDistrictDto district = null;
            if (districtName != null) {
                district = findDistrict(province.getProvinceCode(), districtName);
                if (district == null) {
                    log.warn("District not found: {} in province {}", districtName, provinceName);
                    // Don't return null, continue with what we have
                }
            }

            // Step 3: Find Commune (optional, requires district)
            ClsCommuneDto commune = null;
            if (communeName != null && district != null) {
                commune = findCommune(district.getDistrictCode(), communeName);
                if (commune == null) {
                    log.warn("Commune not found: {} in district {}", communeName, districtName);
                    // Don't return null, continue with what we have
                }
            }

            // POB doesn't have village
            return LocationCodesDto.builder()
                    .province(province)
                    .district(district)
                    .commune(commune)
                    .village(null)
                    .build();

        } catch (Exception ex) {
            log.error("Error resolving POB location: {}", requestDto, ex);
            return null;
        }
    }

    // ---------------------- Code-based lookups ----------------------
    @Override
    public ClsProvinceDto getProvinceByCode(String provinceCode) {
        String sql = "SELECT province_code, province_en, province_kh FROM acc_online_province_cbc WHERE province_code = ? LIMIT 1";
        return jdbcTemplate.query(sql, new ProvinceRowMapper(), provinceCode)
                .stream().findFirst().orElse(null);
    }

    @Override
    public ClsDistrictDto getDistrictByCode(String districtCode) {
        String sql = "SELECT district_code, district_en, district_kh, province_code FROM acc_online_district_cbc WHERE district_code = ? LIMIT 1";
        return jdbcTemplate.query(sql, new DistrictRowMapper(), districtCode)
                .stream().findFirst().orElse(null);
    }

    @Override
    public ClsCommuneDto getCommuneByCode(String communeCode) {
        String sql = "SELECT commune_code, commune_en, commune_kh, district_code FROM acc_online_commune_cbc WHERE commune_code = ? LIMIT 1";
        return jdbcTemplate.query(sql, new CommuneRowMapper(), communeCode)
                .stream().findFirst().orElse(null);
    }

    @Override
    public ClsVillageDto getVillageByCode(String villageCode) {
        String sql = "SELECT village_code, village_en, village_kh, commune_code FROM acc_online_village_cbc WHERE village_code = ? LIMIT 1";
        return jdbcTemplate.query(sql, new VillageRowMapper(), villageCode)
                .stream().findFirst().orElse(null);
    }

    @Override
    public ClsBranchDto getBranchByCode(String branchCode) {
        String sql = "SELECT branch_code, branch_kh FROM acc_online_branch WHERE branch_code = ? LIMIT 1";
        return jdbcTemplate.query(sql, new BranchRowMapper(), branchCode)
                .stream().findFirst().orElse(null);
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
        String sql = "SELECT province_code, province_en, province_kh FROM acc_online_province_cbc " +
                "WHERE province_kh = ? LIMIT 1";
        try {
            List<ClsProvinceDto> results = jdbcTemplate.query(sql,
                    (rs, rowNum) -> ClsProvinceDto.builder()
                            .provinceCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("province_code"), 2))
                            .provinceEn(rs.getString("province_en"))
                            .provinceKh(rs.getString("province_kh"))
                            .build(),
                    provinceName);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("Error finding province for: {}", provinceName, e);
            return null;
        }
    }

    private ClsDistrictDto findDistrict(String provinceCode, String districtName) {
        String sql = "SELECT district_code, district_en, district_kh, province_code " +
                "FROM acc_online_district_cbc " +
                "WHERE province_code = ? AND district_kh = ? LIMIT 1";
        try {
            List<ClsDistrictDto> results = jdbcTemplate.query(sql,
                    (rs, rowNum) -> ClsDistrictDto.builder()
                            .districtCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("district_code"), 4))
                            .districtEn(rs.getString("district_en"))
                            .districtKh(rs.getString("district_kh"))
                            .provinceCode(rs.getString("province_code"))
                            .build(),
                    provinceCode, districtName);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("Error finding district for: {} in province {}", districtName, provinceCode, e);
            return null;
        }
    }

    private ClsCommuneDto findCommune(String districtCode, String communeName) {
        String sql = "SELECT commune_code, commune_en, commune_kh, district_code " +
                "FROM acc_online_commune_cbc " +
                "WHERE district_code = ? AND commune_kh = ? LIMIT 1";
        try {
            List<ClsCommuneDto> results = jdbcTemplate.query(sql,
                    (rs, rowNum) -> ClsCommuneDto.builder()
                            .communeCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("commune_code"), 6))
                            .communeEn(rs.getString("commune_en"))
                            .communeKh(rs.getString("commune_kh"))
                            .districtCode(rs.getString("district_code"))
                            .build(),
                    districtCode, communeName);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("Error finding commune for: {} in district {}", communeName, districtCode, e);
            return null;
        }
    }

    private ClsVillageDto findVillage(String communeCode, String villageName) {
        String sql = "SELECT village_code, village_en, village_kh, commune_code " +
                "FROM acc_online_village_cbc " +
                "WHERE commune_code = ? AND village_kh = ? LIMIT 1";
        try {
            List<ClsVillageDto> results = jdbcTemplate.query(sql,
                    (rs, rowNum) -> ClsVillageDto.builder()
                            .villageCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("village_code"), 8))
                            .villageEn(rs.getString("village_en"))
                            .villageKh(rs.getString("village_kh"))
                            .communeCode(rs.getString("commune_code"))
                            .build(),
                    communeCode, villageName);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("Error finding village for: {} in commune {}", villageName, communeCode, e);
            return null;
        }
    }

    // ---------------- Generic PostgreSQL pagination ----------------
    private <T> PaginationResponse<T> getPaginatedData(
            JdbcTemplate jdbcTemplate,
            AllMasterDataRequest request,
            String tableName,
            RowMapper<T> rowMapper,
            String codeColumn,
            String enColumn,
            String khColumn,
            LocationFilter filter
    ) {
        String search = request.getSearch();
        int pageNo = Math.max(request.getPageNo(), 1) - 1;
        int pageSize = request.getPageSize();

        StringBuilder baseSql = new StringBuilder("SELECT * FROM ").append(tableName);
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);

        List<Object> params = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();
        boolean whereAdded = false;

        // --- Apply dynamic filters ---
        if (filter != null) {
            if (filter.getProvinceCode() != null) {
                baseSql.append(whereAdded ? " AND " : " WHERE ").append("province_code = ?");
                countSql.append(whereAdded ? " AND " : " WHERE ").append("province_code = ?");
                params.add(filter.getProvinceCode());
                countParams.add(filter.getProvinceCode());
                whereAdded = true;
            }
            if (filter.getDistrictCode() != null) {
                baseSql.append(whereAdded ? " AND " : " WHERE ").append("district_code = ?");
                countSql.append(whereAdded ? " AND " : " WHERE ").append("district_code = ?");
                params.add(filter.getDistrictCode());
                countParams.add(filter.getDistrictCode());
                whereAdded = true;
            }
            if (filter.getCommuneCode() != null) {
                baseSql.append(whereAdded ? " AND " : " WHERE ").append("commune_code = ?");
                countSql.append(whereAdded ? " AND " : " WHERE ").append("commune_code = ?");
                params.add(filter.getCommuneCode());
                countParams.add(filter.getCommuneCode());
                whereAdded = true;
            }
        }

        // --- Search ---
        if (search != null && !search.isEmpty()) {
            baseSql.append(whereAdded ? " AND " : " WHERE ")
                    .append("(")
                    .append(codeColumn).append(" ILIKE ? OR ")
                    .append(enColumn).append(" ILIKE ? OR ")
                    .append(khColumn).append(" ILIKE ?)");
            countSql.append(whereAdded ? " AND " : " WHERE ")
                    .append("(")
                    .append(codeColumn).append(" ILIKE ? OR ")
                    .append(enColumn).append(" ILIKE ? OR ")
                    .append(khColumn).append(" ILIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            countParams.addAll(params.subList(params.size() - 3, params.size()));
        }

        baseSql.append(" ORDER BY ").append(codeColumn).append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(pageNo * pageSize);

        return executePaginatedQuery(
                jdbcTemplate,
                baseSql.toString(),
                params.toArray(),
                countSql.toString(),
                countParams.toArray(),
                rowMapper,
                pageNo,
                pageSize,
                tableName
        );
    }

    // ---------------- Execute query ----------------
    private <T> PaginationResponse<T> executePaginatedQuery(
            JdbcTemplate jdbcTemplate,
            String baseSql,
            Object[] baseParams,
            String countSql,
            Object[] countParams,
            RowMapper<T> rowMapper,
            int pageNo,
            int pageSize,
            String entityName
    ) {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            String url = conn.getMetaData().getURL();
            String user = conn.getMetaData().getUserName();
            log.info("[{}] Using connection URL: {} | Username: {}", entityName, url, user);
        } catch (SQLException e) {
            log.warn("Could not log datasource connection for {}", entityName, e);
        }

        try {
            long total = jdbcTemplate.queryForObject(countSql, countParams, Long.class);
            List<T> content = jdbcTemplate.query(baseSql, baseParams, rowMapper);
            return new PaginationResponse<>(content, pageNo + 1, pageSize, total);
        } catch (Exception ex) {
            log.error("Error fetching {}: {}", entityName, ex.getMessage(), ex);
            throw new MasterDataServiceException("Failed to fetch " + entityName, ex);
        }
    }

    // ---------------- RowMappers ----------------
    private static class ProvinceRowMapper implements RowMapper<ClsProvinceDto> {
        @Override
        public ClsProvinceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsProvinceDto.builder()
                    .provinceCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("province_code"), 2))
                    .provinceEn(rs.getString("province_en"))
                    .provinceKh(rs.getString("province_kh"))
                    .build();
        }
    }

    private static class DistrictRowMapper implements RowMapper<ClsDistrictDto> {
        @Override
        public ClsDistrictDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsDistrictDto.builder()
                    .districtCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("district_code"), 4))
                    .districtEn(rs.getString("district_en"))
                    .districtKh(rs.getString("district_kh"))
                    .provinceCode(rs.getString("province_code"))
                    .build();
        }
    }

    private static class CommuneRowMapper implements RowMapper<ClsCommuneDto> {
        @Override
        public ClsCommuneDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsCommuneDto.builder()
                    .communeCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("commune_code"), 6))
                    .communeEn(rs.getString("commune_en"))
                    .communeKh(rs.getString("commune_kh"))
                    .districtCode(rs.getString("district_code"))
                    .build();
        }
    }

    private static class VillageRowMapper implements RowMapper<ClsVillageDto> {
        @Override
        public ClsVillageDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsVillageDto.builder()
                    .villageCode(HelperUtils.formatCodeWithLeadingZero(rs.getString("village_code"), 8))
                    .villageEn(rs.getString("village_en"))
                    .villageKh(rs.getString("village_kh"))
                    .communeCode(rs.getString("commune_code"))
                    .build();
        }
    }

    private static class BranchRowMapper implements RowMapper<ClsBranchDto> {
        @Override
        public ClsBranchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsBranchDto.builder()
                    .branchID(HelperUtils.formatCodeWithLeadingZero(rs.getString("branch_code"), 4))
                    .branchkh(rs.getString("branch_kh"))
                    .build();
        }
    }
}