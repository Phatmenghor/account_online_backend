package com.account_sell.feature.auth.controllers;

import com.account_sell.config.RequiresRole;
import com.account_sell.enumation.StatusData;
import com.account_sell.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.account_sell.feature.auth.dto.request.ChangePasswordRequestDto;
import com.account_sell.feature.auth.dto.request.UpdateUserRequestDto;
import com.account_sell.feature.auth.dto.response.StaffResponseDto;
import com.account_sell.feature.auth.dto.response.UserResponseDto;
import com.account_sell.feature.auth.dto.response.AllUserResponseDto;
import com.account_sell.exceptions.response.ApiResponse;
import com.account_sell.feature.auth.repository.StaffRepository;
import com.account_sell.feature.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for user management operations.
 */
@RestController
@RequestMapping("/api/v1/admin/user")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class UserController {

    private final StaffRepository staffRepository;
    private final UserService userService;

    @PostMapping()
    @RequiresRole(value = {"ADMIN", "DEVELOPER"}, anyRole = true)
    public ApiResponse<AllUserResponseDto> getAllUsers(
            @RequestParam(value = "pageNo", defaultValue = "1", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) StatusData statusData) {

        // Use default values instead of validation
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;

        log.info("Request to get all users - page: {}, size: {}, search: '{}', status: {}",
                effectivePageNo, effectivePageSize, search, statusData);

        // Convert to zero-based page numbering used by Spring Data
        AllUserResponseDto result = userService.getAllUser(effectivePageNo - 1, effectivePageSize, search, statusData);

        log.info("Successfully retrieved {} users (page {}/{})",
                result.getContent().size(), result.getPageNo(), result.getTotalPages());

        return new ApiResponse<>("success", "Users retrieved successfully", result);
    }

    @PostMapping("/getById/{id}")
    public ApiResponse<UserResponseDto> getUserDetail(@PathVariable Long id) {
        log.info("Request to get details for user ID: {}", id);

        UserResponseDto user = userService.getUserById(id);

        log.info("Successfully retrieved details for user ID: {}", id);
        return new ApiResponse<>("success", "User details retrieved successfully", user);
    }

    @PostMapping("/token")
    public ApiResponse<UserResponseDto> getUserByToken() {
        log.info("Request to get current user details from token");

        UserResponseDto user = userService.getUserByToken();

        log.info("Successfully retrieved current user details: {}", user.getEmail());
        return new ApiResponse<>("success", "Current user details retrieved successfully", user);
    }

    @PostMapping("/deleteById/{id}")
    @RequiresRole(value = {"ADMIN", "DEVELOPER"}, anyRole = true)
    public ApiResponse<UserResponseDto> deleteUser(@PathVariable("id") Long userId) {
        log.info("Request to delete user with ID: {}", userId);

        UserResponseDto deletedUser = userService.deleteUserId(userId);

        log.info("Successfully deleted user with ID: {}, username: {}", userId, deletedUser.getEmail());
        return new ApiResponse<>("success", "User deleted successfully", deletedUser);
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<UserResponseDto> updateUser(
            @PathVariable("id") Long userId,
            @RequestBody UpdateUserRequestDto request) {

        log.info("Request to update user with ID: {}, email: {}, status: {}",
                userId, request.getEmail(), request.getStatus());

        UserResponseDto updatedUser = userService.updateUserId(userId, request);

        log.info("Successfully updated user with ID: {}, username: {}", userId, updatedUser.getEmail());
        return new ApiResponse<>("success", "User updated successfully", updatedUser);
    }

    @PostMapping("change-password")
    @RequiresRole(value = {"ADMIN", "DEVELOPER", "USER"}, anyRole = true)
    public ApiResponse<UserResponseDto> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        log.info("Request to change password for current user");

        UserResponseDto userDto = userService.changePassword(changePasswordDto);

        log.info("Successfully changed password for user: {}", userDto.getEmail());
        return new ApiResponse<>("success", "Password changed successfully.", userDto);
    }

    @PostMapping("change-password-by-admin")
    @RequiresRole(value = {"ADMIN", "DEVELOPER"}, anyRole = true)
    public ApiResponse<UserResponseDto> changePasswordByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        log.info("Request by admin to change password for user ID: {}", changePasswordDto.getId());

        UserResponseDto userDto = userService.changePasswordByAdmin(changePasswordDto);

        log.info("Admin successfully changed password for user ID: {}, username: {}",
                changePasswordDto.getId(), userDto.getEmail());
        return new ApiResponse<>("success", "Password changed by admin successfully.", userDto);
    }

    // Endpoint to get staff record by id card
    @PostMapping("/staff/{idCard}")
    public ApiResponse<StaffResponseDto> getStaffById(@PathVariable("idCard") String idCard) {
        log.info("Received request to get staff record");

        try {
            // Call the service method to get the staff record
            StaffResponseDto staff = staffRepository.getStaffByIdCard(idCard);

            log.debug("Staff details: {}", staff);

            // Return the staff record in ApiResponse with HTTP-200 semantics
            return new ApiResponse<>("success", "Staff id card response successfully", staff);

        } catch (SQLException e) {
            log.error("Database error occurred while fetching staff record: {}", e.getMessage(), e);
            return new ApiResponse<>("error", "Database error occurred while fetching staff record", null);

        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching staff record: {}", e.getMessage(), e);
            return new ApiResponse<>("error", "Internal Server Error", null);
        }
    }
}