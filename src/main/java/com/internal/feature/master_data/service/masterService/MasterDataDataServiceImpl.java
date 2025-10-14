package com.internal.feature.master_data.service.masterService;

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

    // ------------------- Province -------------------
    @Override
    public PaginationResponse<ClsProvinceDto> getProvince(AllMasterDataRequest request) {
        try {
            String search = request.getSearch();
            int pageNo = request.getPageNo();
            int pageSize = request.getPageSize();

            String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_PROVINCE";
            Object[] countParams = new Object[]{};
            if (search != null && !search.isEmpty()) {
                countSql += " WHERE PROVINCE_CODE LIKE ? OR PROVINCE_DESC LIKE ?";
                countParams = new Object[]{"%" + search + "%", "%" + search + "%"};
            }
            long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, countParams);

            String sql;
            Object[] params;
            if (search != null && !search.isEmpty()) {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_PROVINCE a " +
                        " WHERE (PROVINCE_CODE LIKE ? OR PROVINCE_DESC LIKE ?) AND ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{"%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize};
            } else {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_PROVINCE a " +
                        " WHERE ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{pageNo * pageSize, (pageNo - 1) * pageSize};
            }

            List<ClsProvinceDto> content = oracleJdbcTemplate.query(sql, new ProvinceRowMapper(), params);
            return new PaginationResponse<>(content, pageNo, pageSize, total);

        } catch (Exception ex) {
            log.error("Error fetching provinces: {}", ex.getMessage(), ex);
            return new PaginationResponse<>(null, request.getPageNo(), request.getPageSize(), 0);
        }
    }

    private static class ProvinceRowMapper implements RowMapper<ClsProvinceDto> {
        @Override
        public ClsProvinceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClsProvinceDto p = new ClsProvinceDto();
            p.setProvinceCode(rs.getString("PROVINCE_CODE"));
            p.setProvinceDesc(rs.getString("PROVINCE_DESC"));
            p.setProvinceDesc2(rs.getString("PROVINCE_DESC2"));
            p.setParentCode(rs.getString("PARENT_CODE"));
            return p;
        }
    }

    // ------------------- District -------------------
    @Override
    public PaginationResponse<ClsDistrictDto> getDistrict(AllMasterDataRequest request, String provinceCode) {
        try {
            String search = request.getSearch();
            int pageNo = request.getPageNo();
            int pageSize = request.getPageSize();

            String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_DISTRICT WHERE PARENT_CODE = ?";
            Object[] countParams = new Object[]{provinceCode};
            if (search != null && !search.isEmpty()) {
                countSql += " AND (DISTRICT_CODE LIKE ? OR DISTRICT_DESC LIKE ?)";
                countParams = new Object[]{provinceCode, "%" + search + "%", "%" + search + "%"};
            }
            long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, countParams);

            String sql;
            Object[] params;
            if (search != null && !search.isEmpty()) {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_DISTRICT a " +
                        " WHERE PARENT_CODE = ? AND (DISTRICT_CODE LIKE ? OR DISTRICT_DESC LIKE ?) AND ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{provinceCode, "%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize};
            } else {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_DISTRICT a " +
                        " WHERE PARENT_CODE = ? AND ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{provinceCode, pageNo * pageSize, (pageNo - 1) * pageSize};
            }

            List<ClsDistrictDto> content = oracleJdbcTemplate.query(sql, new DistrictRowMapper(), params);
            return new PaginationResponse<>(content, pageNo, pageSize, total);

        } catch (Exception ex) {
            log.error("Error fetching districts: {}", ex.getMessage(), ex);
            return new PaginationResponse<>(null, request.getPageNo(), request.getPageSize(), 0);
        }
    }

    private static class DistrictRowMapper implements RowMapper<ClsDistrictDto> {
        @Override
        public ClsDistrictDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClsDistrictDto d = new ClsDistrictDto();
            d.setDistrictCode(rs.getString("DISTRICT_CODE"));
            d.setDistrictDesc(rs.getString("DISTRICT_DESC"));
            d.setDistrictDesc2(rs.getString("DISTRICT_DESC2"));
            d.setParentCode(rs.getString("PARENT_CODE"));
            return d;
        }
    }

    // ------------------- Commune -------------------
    @Override
    public PaginationResponse<ClsCommuneDto> getCommune(AllMasterDataRequest request, String districtCode) {
        try {
            String search = request.getSearch();
            int pageNo = request.getPageNo();
            int pageSize = request.getPageSize();

            String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_COMMUNE WHERE PARENT_CODE = ?";
            Object[] countParams = new Object[]{districtCode};
            if (search != null && !search.isEmpty()) {
                countSql += " AND (COMMUNE_CODE LIKE ? OR COMMUNE_DESC LIKE ?)";
                countParams = new Object[]{districtCode, "%" + search + "%", "%" + search + "%"};
            }
            long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, countParams);

            String sql;
            Object[] params;
            if (search != null && !search.isEmpty()) {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_COMMUNE a " +
                        " WHERE PARENT_CODE = ? AND (COMMUNE_CODE LIKE ? OR COMMUNE_DESC LIKE ?) AND ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{districtCode, "%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize};
            } else {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_COMMUNE a " +
                        " WHERE PARENT_CODE = ? AND ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{districtCode, pageNo * pageSize, (pageNo - 1) * pageSize};
            }

            List<ClsCommuneDto> content = oracleJdbcTemplate.query(sql, new CommuneRowMapper(), params);
            return new PaginationResponse<>(content, pageNo, pageSize, total);

        } catch (Exception ex) {
            log.error("Error fetching communes: {}", ex.getMessage(), ex);
            return new PaginationResponse<>(null, request.getPageNo(), request.getPageSize(), 0);
        }
    }

    private static class CommuneRowMapper implements RowMapper<ClsCommuneDto> {
        @Override
        public ClsCommuneDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClsCommuneDto c = new ClsCommuneDto();
            c.setCommuneCode(rs.getString("COMMUNE_CODE"));
            c.setCommuneDesc(rs.getString("COMMUNE_DESC"));
            c.setCommuneDesc2(rs.getString("COMMUNE_DESC2"));
            c.setParentCode(rs.getString("PARENT_CODE"));
            return c;
        }
    }

    // ------------------- Village -------------------
    @Override
    public PaginationResponse<ClsVillageDto> getVillage(AllMasterDataRequest request, String communeCode) {
        try {
            String search = request.getSearch();
            int pageNo = request.getPageNo();
            int pageSize = request.getPageSize();

            String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_VILLAGE WHERE PARENT_CODE = ?";
            Object[] countParams = new Object[]{communeCode};
            if (search != null && !search.isEmpty()) {
                countSql += " AND (VILLAGE_CODE LIKE ? OR VILLAGE_DESC LIKE ?)";
                countParams = new Object[]{communeCode, "%" + search + "%", "%" + search + "%"};
            }
            long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, countParams);

            String sql;
            Object[] params;
            if (search != null && !search.isEmpty()) {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_VILLAGE a " +
                        " WHERE PARENT_CODE = ? AND (VILLAGE_CODE LIKE ? OR VILLAGE_DESC LIKE ?) AND ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{communeCode, "%" + search + "%", "%" + search + "%", pageNo * pageSize, (pageNo - 1) * pageSize};
            } else {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_VILLAGE a " +
                        " WHERE PARENT_CODE = ? AND ROWNUM <= ? " +
                        ") WHERE rnum > ?";
                params = new Object[]{communeCode, pageNo * pageSize, (pageNo - 1) * pageSize};
            }

            List<ClsVillageDto> content = oracleJdbcTemplate.query(sql, new VillageRowMapper(), params);
            return new PaginationResponse<>(content, pageNo, pageSize, total);

        } catch (Exception ex) {
            log.error("Error fetching villages: {}", ex.getMessage(), ex);
            return new PaginationResponse<>(null, request.getPageNo(), request.getPageSize(), 0);
        }
    }

    private static class VillageRowMapper implements RowMapper<ClsVillageDto> {
        @Override
        public ClsVillageDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClsVillageDto v = new ClsVillageDto();
            v.setVillageCode(rs.getString("VILLAGE_CODE"));
            v.setVillageDesc(rs.getString("VILLAGE_DESC"));
            v.setVillageDesc2(rs.getString("VILLAGE_DESC2"));
            v.setParentCode(rs.getString("PARENT_CODE"));
            return v;
        }
    }

    // ------------------- Branch -------------------
    @Override
    public PaginationResponse<ClsBranchDto> getBranch(AllMasterDataRequest request) {
        try {
            String search = request.getSearch();
            int pageNo = request.getPageNo();
            int pageSize = request.getPageSize();

            // Count total
            String countSql = "SELECT COUNT(*) FROM Branchs WHERE BranchID NOT IN ('HQ','KH0011110')";
            Object[] countParams = new Object[]{};
            if (search != null && !search.isEmpty()) {
                countSql += " AND (BranchID LIKE ? OR Branchkh LIKE ?)";
                countParams = new Object[]{"%" + search + "%", "%" + search + "%"};
            }
            long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, countParams);

            // Oracle pagination with ROWNUM
            String sql;
            Object[] params;
            int startRow = (pageNo - 1) * pageSize + 1;
            int endRow = pageNo * pageSize;

            if (search != null && !search.isEmpty()) {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM (" +
                        "   SELECT * FROM Branchs WHERE BranchID NOT IN ('HQ','KH0011110') AND (BranchID LIKE ? OR Branchkh LIKE ?) ORDER BY BranchID" +
                        " ) a WHERE ROWNUM <= ?" +
                        ") WHERE rnum >= ?";
                params = new Object[]{"%" + search + "%", "%" + search + "%", endRow, startRow};
            } else {
                sql = "SELECT * FROM (" +
                        " SELECT a.*, ROWNUM rnum FROM (" +
                        "   SELECT * FROM Branchs WHERE BranchID NOT IN ('HQ','KH0011110') ORDER BY BranchID" +
                        " ) a WHERE ROWNUM <= ?" +
                        ") WHERE rnum >= ?";
                params = new Object[]{endRow, startRow};
            }

            List<ClsBranchDto> list = oracleJdbcTemplate.query(sql, new BranchRowMapper(), params);
            return new PaginationResponse<>(list, pageNo, pageSize, total);

        } catch (Exception ex) {
            log.error("Error fetching branches: {}", ex.getMessage(), ex);
            return new PaginationResponse<>(null, request.getPageNo(), request.getPageSize(), 0);
        }
    }

    private static class BranchRowMapper implements RowMapper<ClsBranchDto> {
        @Override
        public ClsBranchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClsBranchDto branch = new ClsBranchDto();
            branch.setBranchID(rs.getString("BranchID"));
            branch.setBranchkh(rs.getString("Branchkh"));
            return branch;
        }
    }
}
