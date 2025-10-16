package com.internal.feature.master_data.service.masterService;

import com.internal.exceptions.error.MasterDataServiceException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterDataDataServiceImpl implements MasterDataService {

    @Qualifier("oracleJdbcTemplate")
    private final JdbcTemplate oracleJdbcTemplate;

    /**
     * Centralized method to execute paginated queries
     */
    private <T> PaginationResponse<T> executePaginatedQuery(
            String baseSql,
            String countSql,
            Object[] params,
            RowMapper<T> rowMapper,
            int pageNo,
            int pageSize,
            String entityName
    ) {
        try {
            long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, params);
            List<T> content = oracleJdbcTemplate.query(baseSql, rowMapper, params);
            return new PaginationResponse<>(content, pageNo, pageSize, total);
        } catch (Exception ex) {
            // Log full stack trace and throw custom exception
            log.error("Error fetching {}: {}", entityName, ex.getMessage(), ex);
            throw new MasterDataServiceException("Failed to fetch " + entityName, ex);
        }
    }

    @Override
    public PaginationResponse<ClsProvinceDto> getProvince(AllMasterDataRequest request) {
        String search = request.getSearch();
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        // Build count query
        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_PROVINCE";
        Object[] countParams = new Object[]{};
        if (search != null && !search.isEmpty()) {
            countSql += " WHERE PROVINCE_CODE LIKE ? OR PROVINCE_DESC LIKE ?";
            countParams = new Object[]{"%" + search + "%", "%" + search + "%"};
        }

        // Build pagination query
        String baseSql = search != null && !search.isEmpty()
                ? "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_PROVINCE a " +
                "WHERE (PROVINCE_CODE LIKE ? OR PROVINCE_DESC LIKE ?) AND ROWNUM <= ? ) WHERE rnum > ?"
                : "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_PROVINCE a " +
                "WHERE ROWNUM <= ? ) WHERE rnum > ?";
        Object[] sqlParams = search != null && !search.isEmpty()
                ? new Object[]{"%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize}
                : new Object[]{pageNo * pageSize, (pageNo - 1) * pageSize};

        return executePaginatedQuery(baseSql, countSql, sqlParams, new ProvinceRowMapper(), pageNo, pageSize, "provinces");
    }

    @Override
    public PaginationResponse<ClsDistrictDto> getDistrict(AllMasterDataRequest request, String provinceCode) {
        String search = request.getSearch();
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_DISTRICT WHERE PARENT_CODE = ?" +
                (search != null && !search.isEmpty() ? " AND (DISTRICT_CODE LIKE ? OR DISTRICT_DESC LIKE ?)" : "");
        Object[] countParams = search != null && !search.isEmpty()
                ? new Object[]{provinceCode, "%" + search + "%", "%" + search + "%"}
                : new Object[]{provinceCode};

        String baseSql = search != null && !search.isEmpty()
                ? "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_DISTRICT a " +
                "WHERE PARENT_CODE = ? AND (DISTRICT_CODE LIKE ? OR DISTRICT_DESC LIKE ?) AND ROWNUM <= ?) WHERE rnum > ?"
                : "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_DISTRICT a " +
                "WHERE PARENT_CODE = ? AND ROWNUM <= ?) WHERE rnum > ?";
        Object[] sqlParams = search != null && !search.isEmpty()
                ? new Object[]{provinceCode, "%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize}
                : new Object[]{provinceCode, pageNo * pageSize, (pageNo - 1) * pageSize};

        return executePaginatedQuery(baseSql, countSql, sqlParams, new DistrictRowMapper(), pageNo, pageSize, "districts");
    }

    @Override
    public PaginationResponse<ClsCommuneDto> getCommune(AllMasterDataRequest request, String districtCode) {
        String search = request.getSearch();
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_COMMUNE WHERE PARENT_CODE = ?" +
                (search != null && !search.isEmpty() ? " AND (COMMUNE_CODE LIKE ? OR COMMUNE_DESC LIKE ?)" : "");
        Object[] countParams = search != null && !search.isEmpty()
                ? new Object[]{districtCode, "%" + search + "%", "%" + search + "%"}
                : new Object[]{districtCode};

        String baseSql = search != null && !search.isEmpty()
                ? "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_COMMUNE a " +
                "WHERE PARENT_CODE = ? AND (COMMUNE_CODE LIKE ? OR COMMUNE_DESC LIKE ?) AND ROWNUM <= ?) WHERE rnum > ?"
                : "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_COMMUNE a " +
                "WHERE PARENT_CODE = ? AND ROWNUM <= ?) WHERE rnum > ?";
        Object[] sqlParams = search != null && !search.isEmpty()
                ? new Object[]{districtCode, "%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize}
                : new Object[]{districtCode, pageNo * pageSize, (pageNo - 1) * pageSize};

        return executePaginatedQuery(baseSql, countSql, sqlParams, new CommuneRowMapper(), pageNo, pageSize, "communes");
    }

    @Override
    public PaginationResponse<ClsVillageDto> getVillage(AllMasterDataRequest request, String communeCode) {
        String search = request.getSearch();
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_VILLAGE WHERE PARENT_CODE = ?" +
                (search != null && !search.isEmpty() ? " AND (VILLAGE_CODE LIKE ? OR VILLAGE_DESC LIKE ?)" : "");
        Object[] countParams = search != null && !search.isEmpty()
                ? new Object[]{communeCode, "%" + search + "%", "%" + search + "%"}
                : new Object[]{communeCode};

        String baseSql = search != null && !search.isEmpty()
                ? "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_VILLAGE a " +
                "WHERE PARENT_CODE = ? AND (VILLAGE_CODE LIKE ? OR VILLAGE_DESC LIKE ?) AND ROWNUM <= ?) WHERE rnum > ?"
                : "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_VILLAGE a " +
                "WHERE PARENT_CODE = ? AND ROWNUM <= ?) WHERE rnum > ?";
        Object[] sqlParams = search != null && !search.isEmpty()
                ? new Object[]{communeCode, "%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize}
                : new Object[]{communeCode, pageNo * pageSize, (pageNo - 1) * pageSize};

        return executePaginatedQuery(baseSql, countSql, sqlParams, new VillageRowMapper(), pageNo, pageSize, "villages");
    }

    @Override
    public PaginationResponse<ClsBranchDto> getBranch(AllMasterDataRequest request) {
        String search = request.getSearch();
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        String countSql = "SELECT COUNT(*) FROM Branchs WHERE BranchID NOT IN ('HQ','KH0011110')" +
                (search != null && !search.isEmpty() ? " AND (BranchID LIKE ? OR Branchkh LIKE ?)" : "");
        Object[] countParams = search != null && !search.isEmpty()
                ? new Object[]{"%" + search + "%", "%" + search + "%"}
                : new Object[]{};

        int startRow = (pageNo - 1) * pageSize + 1;
        int endRow = pageNo * pageSize;

        String baseSql = search != null && !search.isEmpty()
                ? "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM Branchs WHERE BranchID NOT IN ('HQ','KH0011110') " +
                "AND (BranchID LIKE ? OR Branchkh LIKE ?) ORDER BY BranchID) a WHERE ROWNUM <= ?) WHERE rnum >= ?"
                : "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM Branchs WHERE BranchID NOT IN ('HQ','KH0011110') " +
                "ORDER BY BranchID) a WHERE ROWNUM <= ?) WHERE rnum >= ?";
        Object[] sqlParams = search != null && !search.isEmpty()
                ? new Object[]{"%" + search + "%", "%" + search + "%", endRow, startRow}
                : new Object[]{endRow, startRow};

        return executePaginatedQuery(baseSql, countSql, sqlParams, new BranchRowMapper(), pageNo, pageSize, "branches");
    }

    // ---------------- RowMappers ----------------
    private static class ProvinceRowMapper implements RowMapper<ClsProvinceDto> {
        @Override
        public ClsProvinceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsProvinceDto.builder()
                    .provinceCode(rs.getString("PROVINCE_CODE"))
                    .provinceDesc(rs.getString("PROVINCE_DESC"))
                    .provinceDesc2(rs.getString("PROVINCE_DESC2"))
                    .parentCode(rs.getString("PARENT_CODE"))
                    .build();
        }
    }

    private static class DistrictRowMapper implements RowMapper<ClsDistrictDto> {
        @Override
        public ClsDistrictDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsDistrictDto.builder()
                    .districtCode(rs.getString("DISTRICT_CODE"))
                    .districtDesc(rs.getString("DISTRICT_DESC"))
                    .districtDesc2(rs.getString("DISTRICT_DESC2"))
                    .parentCode(rs.getString("PARENT_CODE"))
                    .build();
        }
    }

    private static class CommuneRowMapper implements RowMapper<ClsCommuneDto> {
        @Override
        public ClsCommuneDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsCommuneDto.builder()
                    .communeCode(rs.getString("COMMUNE_CODE"))
                    .communeDesc(rs.getString("COMMUNE_DESC"))
                    .communeDesc2(rs.getString("COMMUNE_DESC2"))
                    .parentCode(rs.getString("PARENT_CODE"))
                    .build();
        }
    }

    private static class VillageRowMapper implements RowMapper<ClsVillageDto> {
        @Override
        public ClsVillageDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsVillageDto.builder()
                    .villageCode(rs.getString("VILLAGE_CODE"))
                    .villageDesc(rs.getString("VILLAGE_DESC"))
                    .villageDesc2(rs.getString("VILLAGE_DESC2"))
                    .parentCode(rs.getString("PARENT_CODE"))
                    .build();
        }
    }

    private static class BranchRowMapper implements RowMapper<ClsBranchDto> {
        @Override
        public ClsBranchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClsBranchDto.builder()
                    .branchID(rs.getString("BranchID"))
                    .branchkh(rs.getString("Branchkh"))
                    .build();
        }
    }
}
