package com.internal.feature.auth.repository.impl;

import com.internal.feature.auth.dto.response.StaffResponseDto;
import com.internal.feature.auth.repository.StaffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
@Slf4j
public class StaffRepositoryImpl implements StaffRepository {

    @Value("${spring.datasource.secondary.url}")
    private String dbUrl;

    @Value("${spring.datasource.secondary.username}")
    private String dbUser;

    @Value("${spring.datasource.secondary.password}")
    private String dbPassword;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String dbDriver;

    @Override
    public StaffResponseDto getStaffByCardId(String cardId) throws SQLException {
        log.info("Fetching staff record for card ID: {}", cardId);
        
        validateCardId(cardId);
        
        String query = "SELECT * FROM tblStaffinfo WHERE IDCard = ?";
        
        try (Connection connection = createConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setString(1, cardId.trim());
            
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    StaffResponseDto staff = mapResultSetToStaff(result);
                    log.info("Staff record found for card ID: {}", cardId);
                    return staff;
                } else {
                    log.warn("No staff record found for card ID: {}", cardId);
                    throw new RuntimeException("Staff not found with card ID: " + cardId);
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Database driver not found: {}", dbDriver);
            throw new SQLException("Database driver not found: " + dbDriver, e);
        }
    }

    private void validateCardId(String cardId) {
        if (cardId == null || cardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Card ID cannot be null or empty");
        }
    }

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName(dbDriver);
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private StaffResponseDto mapResultSetToStaff(ResultSet result) throws SQLException {
        return new StaffResponseDto(
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
    }
}