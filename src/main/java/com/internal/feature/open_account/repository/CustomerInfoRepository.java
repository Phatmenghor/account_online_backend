package com.internal.feature.open_account.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class CustomerInfoRepository {

    private final JdbcTemplate stgJdbcTemplate;

    public CustomerInfoRepository(@Qualifier("stgJdbcTemplate") JdbcTemplate stgJdbcTemplate) {
        this.stgJdbcTemplate = stgJdbcTemplate;
    }

    public Map<String, String> findByLegalId(String legalId) {
        try {
            log.debug("Querying customer info for Legal ID: {}", legalId);

            String sql = "SELECT ACCT, MNEMONIC ,CUSTOMERCIF, CUSTOMER_RATING FROM V_CBS_OAO_CUST_CHECK_RATING WHERE legal_id = ?";

            Map<String, String> result = stgJdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, String> map = new HashMap<>();
                map.put("ACCT", rs.getString("ACCT"));
                map.put("MNEMONIC", rs.getString("MNEMONIC"));
                map.put("CIF", rs.getString("CUSTOMERCIF"));
                map.put("RATING", rs.getString("CUSTOMER_RATING"));
                return map;
            }, legalId);

            log.debug("Customer info found - CIF: {}, Rating: {}", result.get("CIF"), result.get("RATING"));
            return result;

        } catch (EmptyResultDataAccessException e) {
            log.debug("No customer found for Legal ID: {}", legalId);
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Error querying customer info for Legal ID {}: {}", legalId, e.getMessage());
            throw e;
        }
    }
}
