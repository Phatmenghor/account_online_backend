package com.account_sell.feature.auth.repository;

import com.account_sell.feature.auth.dto.response.StaffResponseDto;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public interface StaffRepository {
    StaffResponseDto getStaffByIdCard(String idCard) throws SQLException;
}
