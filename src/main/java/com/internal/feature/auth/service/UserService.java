package com.internal.feature.auth.service;

import com.internal.enumation.StatusData;
import com.internal.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.internal.feature.auth.dto.request.ChangePasswordRequestDto;
import com.internal.feature.auth.dto.request.UpdateUserRequestDto;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.dto.response.AllUserResponseDto;

public interface UserService {
    AllUserResponseDto getAllUser(int pageNo, int pageSize, String search, StatusData status);

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByToken();

    UserResponseDto deleteUserId(Long id);

    UserResponseDto updateUserId(Long id, UpdateUserRequestDto request);

    UserResponseDto changePassword(ChangePasswordRequestDto requestDto);

    UserResponseDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto);
}
