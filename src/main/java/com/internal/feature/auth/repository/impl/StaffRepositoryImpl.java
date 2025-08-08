package com.internal.feature.auth.repository.impl;

import com.internal.feature.auth.dto.response.StaffResponseDto;
import com.internal.feature.auth.repository.StaffRepository;
import com.internal.utils.connnection.JdbcInternalConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
@Slf4j
public class StaffRepositoryImpl implements StaffRepository {

    @Autowired
    private JdbcInternalConnection jdbcInternalConnection;

    @Value("${spring.datasource.secondary.url}")
    private String SQL_SERVER_DB_URL;

    @Value("${spring.datasource.secondary.username}")
    private String SQL_SERVER_USER;

    @Value("${spring.datasource.secondary.password}")
    private String SQL_SERVER_PASS;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String SQL_SERVER_CLASS;

    @Override
    public StaffResponseDto getStaffByCardId(String cardId) throws SQLException {
        log.info("=== Starting getStaffByCardId operation ===");
        log.info("Card ID parameter: '{}'", cardId);

        // Debug database configuration
        log.info("Database URL: {}", SQL_SERVER_DB_URL);
        log.info("Database User: {}", SQL_SERVER_USER);
        log.info("Database Driver Class: {}", SQL_SERVER_CLASS);
        log.info("Password configured: {}", (SQL_SERVER_PASS != null && !SQL_SERVER_PASS.isEmpty()) ? "Yes" : "No");

        if (cardId == null || cardId.trim().isEmpty()) {
            log.error("Card ID is null or empty: '{}'", cardId);
            throw new IllegalArgumentException("Card ID cannot be null or empty");
        }

        String queryStr = "SELECT * FROM tblStaffinfo WHERE IDCard = ?";
        log.info("SQL Query: {}", queryStr);
        log.info("Query Parameter: '{}'", cardId);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;

        try {
            log.info("Loading database driver...");
            Class.forName(SQL_SERVER_CLASS);
            log.info("Database driver loaded successfully");

            log.info("Establishing database connection...");
            connection = DriverManager.getConnection(SQL_SERVER_DB_URL, SQL_SERVER_USER, SQL_SERVER_PASS);
            log.info("Database connection established successfully");

            log.info("Preparing SQL statement...");
            preparedStatement = connection.prepareStatement(queryStr);
            preparedStatement.setString(1, cardId.trim());
            log.info("SQL statement prepared with parameter: '{}'", cardId.trim());

            log.info("Executing query...");
            long startTime = System.currentTimeMillis();
            result = preparedStatement.executeQuery();
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Query executed in {} ms", executionTime);

            if (result != null && result.next()) {
                log.info("Staff record found for card ID: {}", cardId);

                // Debug: Log all column values
                try {
                    String name = result.getString("Name");
                    String idCard = result.getString("IDCard");
                    String sex = result.getString("Sex");
                    String status = result.getString("Status");
                    String position = result.getString("Position");
                    String department = result.getString("Department");
                    String location = result.getString("Location");
                    String startingDate = result.getString("StartingDate");
                    String phoneNumber = result.getString("PhoneNumber");
                    String probationDate = result.getString("ProbationDate");
                    String email = result.getString("Email");

                    log.debug("Retrieved values - Name: {}, IDCard: {}, Sex: {}, Status: {}, Position: {}, Department: {}, Location: {}, StartingDate: {}, PhoneNumber: {}, ProbationDate: {}, Email: {}",
                            name, idCard, sex, status, position, department, location, startingDate, phoneNumber, probationDate, email);

                    StaffResponseDto staffResponseDto = new StaffResponseDto(
                            name, idCard, sex, status, position, department, location,
                            startingDate, phoneNumber, probationDate, email
                    );

                    log.info("Successfully created StaffResponseDto for card ID: {}", cardId);
                    return staffResponseDto;

                } catch (SQLException columnException) {
                    log.error("Error reading column data: {}", columnException.getMessage(), columnException);
                    throw new SQLException("Error reading column data: " + columnException.getMessage(), columnException);
                }

            } else {
                log.warn("No staff record found for card ID: {}", cardId);
                throw new RuntimeException("Staff not found with card ID: " + cardId);
            }

        } catch (ClassNotFoundException e) {
            log.error("Database driver not found: {}", SQL_SERVER_CLASS, e);
            throw new SQLException("Database driver not found: " + SQL_SERVER_CLASS, e);

        } catch (SQLException e) {
            log.error("SQL Exception - Error Code: {}, SQL State: {}, Message: {}",
                    e.getErrorCode(), e.getSQLState(), e.getMessage(), e);
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error in getStaffByCardId: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing staff record for card ID: " + cardId, e);

        } finally {
            // Clean up resources
            log.info("Cleaning up database resources...");
            try {
                if (result != null) {
                    result.close();
                    log.debug("Closed ResultSet");
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                    log.debug("Closed PreparedStatement");
                }
                if (connection != null) {
                    connection.close();
                    log.debug("Closed Connection");
                }
                log.info("Database resources cleaned up successfully");
            } catch (SQLException e) {
                log.warn("Error closing database resources: {}", e.getMessage());
            }
        }
    }
}