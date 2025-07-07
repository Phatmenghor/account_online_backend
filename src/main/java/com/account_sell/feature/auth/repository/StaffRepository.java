package com.account_sell.feature.auth.repository;

import com.account_sell.feature.auth.dto.response.StaffResponseDto;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public interface StaffRepository {
    StaffResponseDto getStaffByCardId(String cardId) throws SQLException;
}
