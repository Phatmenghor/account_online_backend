package com.account_sell.feature.auth.service.impl;

import com.account_sell.exceptions.error.NotFoundException;
import com.account_sell.feature.auth.dto.response.StaffResponseDto;
import com.account_sell.feature.auth.repository.StaffRepository;
import com.account_sell.utils.connnection.JdbcInternalConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Slf4j
public class StaffServiceImpl implements StaffRepository {

    @Autowired
    private JdbcInternalConnection jdbcInternalConnection;

    @Override
    public StaffResponseDto getStaffByIdCard(String idCard) throws SQLException {
        log.info("Starting get Staff by id card from tblStaffinfo");

        // Same query helper; still uses string substitution (replace later with PreparedStatement)
        String queryStr = String.format(
                "SELECT * FROM tblStaffinfo WHERE IDCard = '%s'", idCard);
        log.debug("Executing SQL query: {}", queryStr);

        try {
            ResultSet result = jdbcInternalConnection.getData(queryStr);
            log.debug("Successfully executed database query");

            StaffResponseDto staff = null;

            if (result != null && result.next()) {
                staff = new StaffResponseDto(
                        result.getString("Name"),
                        result.getString("IDCard"),
                        result.getString("Sex"),
                        result.getString("Status"),
                        result.getString("Position"),
                        result.getString("Department"),
                        result.getString("Location"),
                        result.getString("StartingDate"),
                        result.getString("PhoneNumber"),
                        result.getString("ProbationDate"),
                        result.getString("Email")
                );

                if (result.next()) {          // more than one row ⇒ log but still return first
                    log.warn("Multiple records found for IDCard {} – returning the first one", idCard);
                }
            }

            if (staff == null) {
                log.warn("No staff record found for IDCard {}", idCard);
                throw new NotFoundException("Staff with IDCard " + idCard + " not found");
            }

            log.info("Successfully retrieved staff record for IDCard {}", idCard);
            return staff;

        } catch (SQLException e) {
            log.error("Database error while fetching staff record: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while processing staff record: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing staff record", e);
        }
    }
}