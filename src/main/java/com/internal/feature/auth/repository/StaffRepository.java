package com.internal.feature.auth.repository;

import com.internal.feature.auth.dto.response.StaffResponseDto;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public interface StaffRepository {
    StaffResponseDto getStaffByCardId(String cardId) throws SQLException;
}
