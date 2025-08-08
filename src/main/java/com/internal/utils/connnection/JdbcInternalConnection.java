package com.internal.utils.connnection;

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

    /**
     * Execute a parameterized query with proper resource management
     * @param query SQL query with parameters
     * @param cardId Parameter value for the query
     * @return ResultSet with query results
     * @throws SQLException if database error occurs
     */
    public ResultSet getDataByCardId(String query, String cardId) throws SQLException {
        log.info("Starting database connection to secondary datasource for card ID: {}", cardId);
        log.debug("Database driver: {}", SQL_SERVER_CLASS);
        log.debug("Database URL: {}", SQL_SERVER_DB_URL);
        log.debug("Database user: {}", SQL_SERVER_USER);

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            log.debug("Loading database driver: {}", SQL_SERVER_CLASS);
            Class.forName(SQL_SERVER_CLASS);
            log.debug("Database driver loaded successfully");

            log.info("Establishing connection to database");
            connection = DriverManager.getConnection(SQL_SERVER_DB_URL, SQL_SERVER_USER, SQL_SERVER_PASS);
            log.info("Database connection established successfully");

            log.debug("Preparing parameterized SQL statement");
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, cardId);

            log.info("Executing query with card ID: {}", cardId);

            long startTime = System.currentTimeMillis();
            ResultSet result = preparedStatement.executeQuery();
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Query executed successfully in {} ms", executionTime);

            return result;

        } catch (ClassNotFoundException e) {
            log.error("Database driver not found: {}", SQL_SERVER_CLASS, e);

            // Clean up resources on error
            cleanup(connection, preparedStatement, null);
            throw new SQLException("Database driver not found: " + SQL_SERVER_CLASS, e);

        } catch (SQLException e) {
            log.error("SQL error occurred - Code: {}, State: {}, Message: {}",
                    e.getErrorCode(), e.getSQLState(), e.getMessage(), e);

            // Clean up resources on error
            cleanup(connection, preparedStatement, null);
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error during database operation: {}", e.getMessage(), e);

            // Clean up resources on error
            cleanup(connection, preparedStatement, null);
            throw new SQLException("Unexpected error during database operation", e);
        }
    }

    /**
     * Get a database connection
     * @return Connection to the secondary database
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName(SQL_SERVER_CLASS);
            return DriverManager.getConnection(SQL_SERVER_DB_URL, SQL_SERVER_USER, SQL_SERVER_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + SQL_SERVER_CLASS, e);
        }
    }

    /**
     * Clean up database resources safely
     * @param connection Database connection
     * @param statement Prepared statement
     * @param resultSet Result set
     */
    public void cleanup(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
                log.debug("Closed ResultSet");
            }
        } catch (SQLException e) {
            log.warn("Error closing ResultSet: {}", e.getMessage());
        }

        try {
            if (statement != null) {
                statement.close();
                log.debug("Closed PreparedStatement");
            }
        } catch (SQLException e) {
            log.warn("Error closing PreparedStatement: {}", e.getMessage());
        }

        try {
            if (connection != null) {
                connection.close();
                log.debug("Closed Connection");
            }
        } catch (SQLException e) {
            log.warn("Error closing Connection: {}", e.getMessage());
        }
    }

    /**
     * Legacy method - kept for backward compatibility but deprecated
     * @deprecated Use getDataByCardId or getConnection instead for better resource management
     */
    @Deprecated
    public ResultSet getData(String query) throws SQLException {
        log.warn("Using deprecated getData method. Consider using parameterized queries for better security.");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            Class.forName(SQL_SERVER_CLASS);
            connection = DriverManager.getConnection(SQL_SERVER_DB_URL, SQL_SERVER_USER, SQL_SERVER_PASS);
            preparedStatement = connection.prepareStatement(query);
            return preparedStatement.executeQuery();

        } catch (ClassNotFoundException e) {
            cleanup(connection, preparedStatement, null);
            throw new SQLException("Database driver not found: " + SQL_SERVER_CLASS, e);
        } catch (SQLException e) {
            cleanup(connection, preparedStatement, null);
            throw e;
        }
    }
}