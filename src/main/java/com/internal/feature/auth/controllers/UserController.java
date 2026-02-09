package com.internal.feature.auth.controllers;

import com.internal.config.RequiresRole;
import com.internal.exceptions.response.ApiResponse;
import com.internal.feature.auth.dto.request.*;
import com.internal.feature.auth.dto.response.AllUserResponseDto;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.service.AuthService;
import com.internal.feature.auth.service.UserService;
import com.internal.utils.constants.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<AllUserResponseDto>> getAllUsers(@RequestBody GetAllUserRequestDto request) {
        log.info("Fetching users - page: {}, size: {}, search: '{}', status: {}",
                request.getPageNo(), request.getPageSize(), request.getSearch(), request.getStatus());
        AllUserResponseDto result = userService.getAllUser(request);
        log.info("Successfully retrieved {} users (page {}/{})", result.getContent().size(), result.getPageNo(), result.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERS_RETRIEVED, result));
    }

    @PostMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserDetail(@PathVariable Long id) {
        log.info("Fetching user details for ID: {}", id);
        UserResponseDto user = userService.getUserById(id);
        log.info("Successfully retrieved user details for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_DETAIL_RETRIEVED, user));
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByToken() {
        log.debug("Fetching current user from token");
        UserResponseDto user = userService.getUserByToken();
        log.debug("Successfully retrieved current user: {}", user.getIdCard());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CURRENT_USER_RETRIEVED, user));
    }

    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Admin user creation request for ID card: {}", registerDto.getUsername());
        UserResponseDto userResponse = authService.createUserByAdmin(registerDto);
        log.info("Admin user creation successful for: {}", registerDto.getUsername());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_CREATED, userResponse));
    }

    @PostMapping("/deleteById/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> deleteUser(@PathVariable("id") Long userId) {
        log.info("Deleting user with ID: {}", userId);
        UserResponseDto deletedUser = userService.deleteUserId(userId);
        log.info("Successfully deleted user: {} (ID: {})", deletedUser.getIdCard(), userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_DELETED, deletedUser));
    }

    @PostMapping("/updateById/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable("id") Long userId,
                                                                   @RequestBody UpdateUserRequestDto request) {
        log.info("Updating user ID: {} with data: {}", userId, request);
        UserResponseDto updatedUser = userService.updateUserId(userId, request);
        log.info("Successfully updated user: {} (ID: {})", updatedUser.getIdCard(), userId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_UPDATED, updatedUser));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<UserResponseDto>> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        log.info("Password change request for current user");
        UserResponseDto userDto = userService.changePassword(changePasswordDto);
        log.info("Successfully changed password for user: {}", userDto.getIdCard());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PASSWORD_CHANGED, userDto));
    }

    @PostMapping("/change-password-by-admin")
    public ResponseEntity<ApiResponse<UserResponseDto>> changePasswordByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        log.info("Admin password change request for user ID: {}", changePasswordDto.getId());
        UserResponseDto userDto = userService.changePasswordByAdmin(changePasswordDto);
        log.info("Admin successfully changed password for user ID: {}, username: {}", changePasswordDto.getId(), userDto.getIdCard());
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.PASSWORD_CHANGED_BY_ADMIN, userDto));
    }
}
