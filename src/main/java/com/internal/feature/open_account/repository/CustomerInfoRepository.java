
package com.internal.feature.open_account.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomerInfoRepository {

    private final DataSource oracleDataSource;

    public Map<String, String> findByLegalId(String legalId) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(oracleDataSource);
            String sql = "SELECT ACCT, CUSTOMERCIF, CUSTOMER_RATING FROM V_CBS_OAO_CUST_CHECK_RATING WHERE legal_id = ?";
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, String> result = new HashMap<>();
                result.put("ACCT", rs.getString("ACCT"));
                result.put("CIF", rs.getString("CUSTOMERCIF"));
                result.put("RATING", rs.getString("CUSTOMER_RATING"));
                return result;
            }, legalId);
            
        } catch (EmptyResultDataAccessException e) {
            return new HashMap<>();
        }
    }

    public void testConnection() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(oracleDataSource);
        jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
    }
}