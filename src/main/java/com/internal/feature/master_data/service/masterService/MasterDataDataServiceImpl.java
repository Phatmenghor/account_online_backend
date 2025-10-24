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
    @Qualifier("jdbcTemplate")
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
                "acc_online_branch",       // replace with your branch table name
                new BranchRowMapper(),
                "branch_code",          // branch code column
                null,            // branch name English column
                "branch_kh",            // branch name Khmer column
                null                    // no parent filter
        );
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
