package com.internal.feature.master_data.service.masterService;

import com.internal.exceptions.error.MasterDataServiceException;
import com.internal.feature.master_data.dto.request.AllMasterDataRequest;
import com.internal.feature.master_data.dto.response.*;
import com.internal.feature.master_data.service.MasterDataService;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${masterdata.schema:STG}") // Default to STG if not specified
    private String masterDataSchema;

    /**
     * Centralized method to execute paginated queries
     */
    private <T> PaginationResponse<T> executePaginatedQuery(
            String baseSql,
            Object[] baseParams,
            String countSql,
            Object[] countParams,
            RowMapper<T> rowMapper,
            int pageNo,
            int pageSize,
            String entityName
    ) {
        try {
            long total = oracleJdbcTemplate.queryForObject(countSql, countParams, Long.class);
            List<T> content = oracleJdbcTemplate.query(baseSql, baseParams, rowMapper);
            return new PaginationResponse<>(content, pageNo, pageSize, total);
        } catch (Exception ex) {
            log.error("Error fetching {}: {}", entityName, ex.getMessage(), ex);
            throw new MasterDataServiceException("Failed to fetch " + entityName, ex);
        }
    }

    @Override
    public PaginationResponse<ClsProvinceDto> getProvince(AllMasterDataRequest request) {
        return getPaginatedData(
                request,
                masterDataSchema + ".D_CBS_ADDRESS_PROVINCE",
                new ProvinceRowMapper(),
                "PROVINCE_CODE",
                "PROVINCE_DESC",
                "provinces",
                null
        );
    }

    @Override
    public PaginationResponse<ClsDistrictDto> getDistrict(AllMasterDataRequest request, String provinceCode) {
        return getPaginatedData(
                request,
                masterDataSchema + ".D_CBS_ADDRESS_DISTRICT",
                new DistrictRowMapper(),
                "DISTRICT_CODE",
                "DISTRICT_DESC",
                "districts",
                provinceCode
        );
    }

    @Override
    public PaginationResponse<ClsCommuneDto> getCommune(AllMasterDataRequest request, String districtCode) {
        return getPaginatedData(
                request,
                masterDataSchema + ".D_CBS_ADDRESS_COMMUNE",
                new CommuneRowMapper(),
                "COMMUNE_CODE",
                "COMMUNE_DESC",
                "communes",
                districtCode
        );
    }

    @Override
    public PaginationResponse<ClsVillageDto> getVillage(AllMasterDataRequest request, String communeCode) {
        return getPaginatedData(
                request,
                masterDataSchema + ".D_CBS_ADDRESS_VILLAGE",
                new VillageRowMapper(),
                "VILLAGE_CODE",
                "VILLAGE_DESC",
                "villages",
                communeCode
        );
    }

    @Override
    public PaginationResponse<ClsBranchDto> getBranch(AllMasterDataRequest request) {
        String search = request.getSearch();
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        String countSql = "SELECT COUNT(*) FROM Branchs WHERE BranchID NOT IN ('HQ','KH0011110')";
        Object[] countParams;
        String baseSql;
        Object[] baseParams;

        int startRow = (pageNo - 1) * pageSize + 1;
        int endRow = pageNo * pageSize;

        if (search != null && !search.isEmpty()) {
            countSql += " AND (BranchID LIKE ? OR Branchkh LIKE ?)";
            countParams = new Object[]{"%" + search + "%", "%" + search + "%"};
            baseSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM Branchs " +
                    "WHERE BranchID NOT IN ('HQ','KH0011110') AND (BranchID LIKE ? OR Branchkh LIKE ?) " +
                    "ORDER BY BranchID) a WHERE ROWNUM <= ?) WHERE rnum >= ?";
            baseParams = new Object[]{"%" + search + "%", "%" + search + "%", endRow, startRow};
        } else {
            countParams = new Object[]{};
            baseSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM Branchs " +
                    "WHERE BranchID NOT IN ('HQ','KH0011110') ORDER BY BranchID) a WHERE ROWNUM <= ?) WHERE rnum >= ?";
            baseParams = new Object[]{endRow, startRow};
        }

        return executePaginatedQuery(baseSql, baseParams, countSql, countParams, new BranchRowMapper(), pageNo, pageSize, "branches");
    }

    /**
     * Generic method for paginated address entities
     */
    private <T> PaginationResponse<T> getPaginatedData(
            AllMasterDataRequest request,
            String tableName,
            RowMapper<T> rowMapper,
            String codeColumn,
            String descColumn,
            String entityName,
            String parentCode
    ) {
        String search = request.getSearch();
        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();

        String countSql = "SELECT COUNT(*) FROM " + tableName;
        Object[] countParams = parentCode != null ? new Object[]{parentCode} : new Object[]{};
        String baseSql;
        Object[] baseParams;

        if (parentCode != null) {
            countSql += " WHERE PARENT_CODE = ?";
        }

        if (search != null && !search.isEmpty()) {
            if (parentCode != null) {
                countSql += " AND (" + codeColumn + " LIKE ? OR " + descColumn + " LIKE ?)";
                countParams = new Object[]{parentCode, "%" + search + "%", "%" + search + "%"};
                baseSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM " + tableName + " a " +
                        "WHERE PARENT_CODE = ? AND (" + codeColumn + " LIKE ? OR " + descColumn + " LIKE ?) " +
                        "AND ROWNUM <= ?) WHERE rnum > ?";
                baseParams = new Object[]{parentCode, "%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize};
            } else {
                countSql += " WHERE " + codeColumn + " LIKE ? OR " + descColumn + " LIKE ?";
                countParams = new Object[]{"%" + search + "%", "%" + search + "%"};
                baseSql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM " + tableName + " a " +
                        "WHERE " + codeColumn + " LIKE ? OR " + descColumn + " LIKE ? AND ROWNUM <= ?) WHERE rnum > ?";
                baseParams = new Object[]{"%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize};
            }
        } else {
            baseSql = parentCode != null
                    ? "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM " + tableName + " a WHERE PARENT_CODE = ? AND ROWNUM <= ?) WHERE rnum > ?"
                    : "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM " + tableName + " a WHERE ROWNUM <= ?) WHERE rnum > ?";
            baseParams = parentCode != null ? new Object[]{parentCode, pageNo * pageSize, (pageNo - 1) * pageSize} : new Object[]{pageNo * pageSize, (pageNo - 1) * pageSize};
        }

        return executePaginatedQuery(baseSql, baseParams, countSql, countParams, rowMapper, pageNo, pageSize, entityName);
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
