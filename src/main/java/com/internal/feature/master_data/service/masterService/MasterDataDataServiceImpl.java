package com.internal.feature.master_data.service.masterService;

import com.internal.exceptions.error.MasterDataServiceException;
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
    public LocationCodesDto initAddress(String fullLocationString) {
        if (fullLocationString == null || fullLocationString.trim().isEmpty()) {
            return null;
        }

        try {
            // Parse the location string (format: "ភូមិក្រពើ ឃុំតំលោក ស្រុកពញាក្រែក តាកែវ")
            String[] parts = fullLocationString.trim().split("\\s+");

            if (parts.length < 4) {
                log.warn("Invalid location format: {}", fullLocationString);
                return null;
            }

            // Extract names by removing Khmer prefixes
            String villageName = removePrefix(parts[0], "ភូមិ");
            String communeName = removePrefix(parts[1], "ឃុំ", "សង្កាត់");
            String districtName = removePrefix(parts[2], "ស្រុក", "ក្រុង", "ខណ្ឌ");
            String provinceName = parts[3]; // Province has no prefix

            log.info("Resolving location - Province: {}, District: {}, Commune: {}, Village: {}",
                    provinceName, districtName, communeName, villageName);

            // Step 1: Find Province
            ClsProvinceDto province = findProvince(provinceName);
            if (province == null) {
                log.warn("Province not found: {}", provinceName);
                return null;
            }

            // Step 2: Find District
            ClsDistrictDto district = findDistrict(province.getProvinceCode(), districtName);
            if (district == null) {
                log.warn("District not found: {} in province {}", districtName, provinceName);
                return null;
            }

            // Step 3: Find Commune
            ClsCommuneDto commune = findCommune(district.getDistrictCode(), communeName);
            if (commune == null) {
                log.warn("Commune not found: {} in district {}", communeName, districtName);
                return null;
            }

            // Step 4: Find Village
            ClsVillageDto village = findVillage(commune.getCommuneCode(), villageName);
            if (village == null) {
                log.warn("Village not found: {} in commune {}", villageName, communeName);
                return null;
            }

            return LocationCodesDto.builder()
                    .province(province)
                    .district(district)
                    .commune(commune)
                    .village(village)
                    .build();

        } catch (Exception ex) {
            log.error("Error resolving location: {}", fullLocationString, ex);
            return null;
        }
    }

    // ---------------------- POB Resolution (No Village) ----------------------
    @Override
    public LocationCodesDto initPob(String pobString) {
        if (pobString == null || pobString.trim().isEmpty()) {
            return null;
        }

        try {
            // Parse the POB string (format: "ឃុំតំលោក ស្រុកពញាក្រែក តាកែវ")
            String[] parts = pobString.trim().split("\\s+");

            if (parts.length < 3) {
                log.warn("Invalid POB format: {}", pobString);
                return null;
            }

            // Extract names by removing Khmer prefixes
            String communeName = removePrefix(parts[0], "ឃុំ", "សង្កាត់");
            String districtName = removePrefix(parts[1], "ស្រុក", "ក្រុង", "ខណ្ឌ");
            String provinceName = parts[2]; // Province has no prefix

            log.info("Resolving POB - Province: {}, District: {}, Commune: {}",
                    provinceName, districtName, communeName);

            // Step 1: Find Province
            ClsProvinceDto province = findProvince(provinceName);
            if (province == null) {
                log.warn("Province not found: {}", provinceName);
                return null;
            }

            // Step 2: Find District
            ClsDistrictDto district = findDistrict(province.getProvinceCode(), districtName);
            if (district == null) {
                log.warn("District not found: {} in province {}", districtName, provinceName);
                return null;
            }

            // Step 3: Find Commune
            ClsCommuneDto commune = findCommune(district.getDistrictCode(), communeName);
            if (commune == null) {
                log.warn("Commune not found: {} in district {}", communeName, districtName);
                return null;
            }

            // POB doesn't have village, so return without it
            return LocationCodesDto.builder()
                    .province(province)
                    .district(district)
                    .commune(commune)
                    .village(null)
                    .build();

        } catch (Exception ex) {
            log.error("Error resolving POB location: {}", pobString, ex);
            return null;
        }
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