package com.internal.feature.master_data.service.masterService;

import com.internal.feature.master_data.dto.response.ClsCommuneDto;
import com.internal.feature.master_data.dto.response.ClsDistrictDto;
import com.internal.feature.master_data.dto.response.ClsProvinceDto;
import com.internal.feature.master_data.dto.response.ClsVillageDto;
import com.internal.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterDataDataServiceImpl implements MasterDataService {

    @Qualifier("oracleJdbcTemplate")
    private final JdbcTemplate oracleJdbcTemplate;

    @Override
    public PaginationResponse<ClsProvinceDto> getProvince(int pageNo, int pageSize) {
        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_PROVINCE";
        long total = oracleJdbcTemplate.queryForObject(countSql, Long.class);

        String sql = "SELECT * FROM (" +
                " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_PROVINCE a WHERE ROWNUM <= ?" +
                ") WHERE rnum > ?";

        List<ClsProvinceDto> content = oracleJdbcTemplate.query(sql, (rs, rowNum) -> {
            ClsProvinceDto p = new ClsProvinceDto();
            p.setProvinceCode(rs.getString("PROVINCE_CODE"));
            p.setProvinceDesc(rs.getString("PROVINCE_DESC"));
            p.setProvinceDesc2(rs.getString("PROVINCE_DESC2"));
            p.setParentCode(rs.getString("PARENT_CODE"));
            return p;
        }, pageNo * pageSize, (pageNo - 1) * pageSize);

        return new PaginationResponse<>(content, pageNo, pageSize, total);
    }

    @Override
    public PaginationResponse<ClsDistrictDto> getDistrict(String provinceCode, int pageNo, int pageSize) {
        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_DISTRICT WHERE PARENT_CODE = ?";
        long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, provinceCode);

        String sql = "SELECT * FROM (" +
                " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_DISTRICT a WHERE PARENT_CODE = ? AND ROWNUM <= ?" +
                ") WHERE rnum > ?";

        List<ClsDistrictDto> content = oracleJdbcTemplate.query(sql, (rs, rowNum) -> {
            ClsDistrictDto d = new ClsDistrictDto();
            d.setDistrictCode(rs.getString("DISTRICT_CODE"));
            d.setDistrictDesc(rs.getString("DISTRICT_DESC"));
            d.setDistrictDesc2(rs.getString("DISTRICT_DESC2"));
            d.setParentCode(rs.getString("PARENT_CODE"));
            return d;
        }, provinceCode, pageNo * pageSize, (pageNo - 1) * pageSize);

        return new PaginationResponse<>(content, pageNo, pageSize, total);
    }

    @Override
    public PaginationResponse<ClsCommuneDto> getCommune(String districtCode, int pageNo, int pageSize) {
        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_COMMUNE WHERE PARENT_CODE = ?";
        long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, districtCode);

        String sql = "SELECT * FROM (" +
                " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_COMMUNE a WHERE PARENT_CODE = ? AND ROWNUM <= ?" +
                ") WHERE rnum > ?";

        List<ClsCommuneDto> content = oracleJdbcTemplate.query(sql, (rs, rowNum) -> {
            ClsCommuneDto c = new ClsCommuneDto();
            c.setCommuneCode(rs.getString("COMMUNE_CODE"));
            c.setCommuneDesc(rs.getString("COMMUNE_DESC"));
            c.setCommuneDesc2(rs.getString("COMMUNE_DESC2"));
            c.setParentCode(rs.getString("PARENT_CODE"));
            return c;
        }, districtCode, pageNo * pageSize, (pageNo - 1) * pageSize);

        return new PaginationResponse<>(content, pageNo, pageSize, total);
    }

    @Override
    public PaginationResponse<ClsVillageDto> getVillage(String communeCode, int pageNo, int pageSize) {
        String countSql = "SELECT COUNT(*) FROM D_CBS_ADDRESS_VILLAGE WHERE PARENT_CODE = ?";
        long total = oracleJdbcTemplate.queryForObject(countSql, Long.class, communeCode);

        String sql = "SELECT * FROM (" +
                " SELECT a.*, ROWNUM rnum FROM D_CBS_ADDRESS_VILLAGE a WHERE PARENT_CODE = ? AND ROWNUM <= ?" +
                ") WHERE rnum > ?";

        List<ClsVillageDto> content = oracleJdbcTemplate.query(sql, (rs, rowNum) -> {
            ClsVillageDto v = new ClsVillageDto();
            v.setVillageCode(rs.getString("VILLAGE_CODE"));
            v.setVillageDesc(rs.getString("VILLAGE_DESC"));
            v.setVillageDesc2(rs.getString("VILLAGE_DESC2"));
            v.setParentCode(rs.getString("PARENT_CODE"));
            return v;
        }, communeCode, pageNo * pageSize, (pageNo - 1) * pageSize);

        return new PaginationResponse<>(content, pageNo, pageSize, total);
    }
}
