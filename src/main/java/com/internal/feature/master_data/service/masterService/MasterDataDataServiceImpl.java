package com.internal.feature.master_data.service.masterService;

import com.internal.feature.master_data.dto.response.ClsProvinceResponse;
import com.internal.feature.master_data.model.ClsProvince;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterServiceImpl implements masterService {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public ClsProvinceResponse getProvince() {
        ClsProvinceResponse response = new ClsProvinceResponse();

        try {
            String sql = "SELECT * FROM D_CBS_ADDRESS_PROVINCE";

            List<ClsProvince> provinces = jdbcTemplate.query(sql, new RowMapper<ClsProvince>() {
                @Override
                public ClsProvince mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ClsProvince p = new ClsProvince();
                    p.setProvinceCode(rs.getString("PROVINCE_CODE"));
                    p.setProvinceDesc(rs.getString("PROVINCE_DESC"));
                    p.setProvinceDesc2(rs.getString("PROVINCE_DESC2"));
                    p.setParentCode(rs.getString("PARENT_CODE"));
                    return p;
                }
            });

            response.setLocations(provinces);
            response.setErrCode(0);
            response.setErrMsg("Your request success.");
            return response;

        } catch (Exception e) {
            response.setErrCode(-1);
            response.setErrMsg(e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
