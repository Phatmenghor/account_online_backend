package com.account_sell.utils.connnection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
@Slf4j
public class JdbcInternalConnection {
    @Value("${spring.datasource.secondary.driver-class-name}")
    private String SQL_SERVER_CLASS;

    @Value("${spring.datasource.secondary.url}")
    private String SQL_SERVER_DB_URL;

    @Value("${spring.datasource.secondary.username}")
    private String SQL_SERVER_USER;

    @Value("${spring.datasource.secondary.password}")
    private String SQL_SERVER_PASS;

    public ResultSet getData(String query) throws SQLException {
        log.info("Starting database connection to secondary datasource");
        log.debug("Database driver: {}", SQL_SERVER_CLASS);
        log.debug("Database URL: {}", SQL_SERVER_DB_URL);
        log.debug("Database user: {}", SQL_SERVER_USER);
        log.debug("Password configured: {}", SQL_SERVER_PASS != null && !SQL_SERVER_PASS.isEmpty() ? "Yes" : "No");

        ResultSet result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            log.debug("Loading database driver: {}", SQL_SERVER_CLASS);
            Class.forName(SQL_SERVER_CLASS);
            log.debug("Database driver loaded successfully");

            log.info("Establishing connection to database");
            connection = DriverManager.getConnection(SQL_SERVER_DB_URL, SQL_SERVER_USER, SQL_SERVER_PASS);
            log.info("Database connection established successfully");

            log.debug("Preparing SQL statement");
            preparedStatement = connection.prepareStatement(query);
            log.info("Executing query: {}", query);

            long startTime = System.currentTimeMillis();
            result = preparedStatement.executeQuery();
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Query executed successfully in {} ms", executionTime);

            if (result != null) {
                log.debug("ResultSet created successfully");
            } else {
                log.warn("Query returned null ResultSet");
            }

        } catch (ClassNotFoundException e) {
            log.error("Database driver not found: {}", SQL_SERVER_CLASS, e);
            throw new SQLException("Database driver not found: " + SQL_SERVER_CLASS, e);
        } catch (SQLException e) {
            log.error("SQL error occurred - Code: {}, State: {}, Message: {}",
                    e.getErrorCode(), e.getSQLState(), e.getMessage(), e);

            if (connection != null) {
                try {
                    connection.close();
                    log.debug("Closed connection due to SQL error");
                } catch (SQLException closeEx) {
                    log.warn("Error closing connection: {}", closeEx.getMessage());
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                    log.debug("Closed prepared statement due to SQL error");
                } catch (SQLException closeEx) {
                    log.warn("Error closing prepared statement: {}", closeEx.getMessage());
                }
            }

            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during database operation: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error during database operation", e);
        }

        log.info("Returning ResultSet to caller");
        log.warn("Note: Connection and statement resources are not closed - caller must handle cleanup");

        return result;
    }
}