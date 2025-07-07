package com.account_sell.feature.auth.service.impl;

import com.account_sell.enumation.StatusData;
import com.account_sell.exceptions.error.BadRequestException;
import com.account_sell.exceptions.error.DuplicateNameException;
import com.account_sell.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.account_sell.feature.auth.dto.request.ChangePasswordRequestDto;
import com.account_sell.feature.auth.dto.request.UpdateUserRequestDto;
import com.account_sell.feature.auth.dto.response.UserResponseDto;
import com.account_sell.feature.auth.dto.response.AllUserResponseDto;
import com.account_sell.feature.auth.mapper.UserMapper;
import com.account_sell.feature.auth.models.UserEntity;
import com.account_sell.feature.auth.repository.UserRepository;
import com.account_sell.exceptions.error.NotFoundException;
import com.account_sell.feature.auth.service.UserService;
import com.account_sell.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public AllUserResponseDto getAllUser(int pageNo, int pageSize,
                                         String search, StatusData status) {

        log.info("Getting users with pageNo={}, pageSize={}", pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<UserEntity> userPage;

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasStatus = status != null;

        if (!hasStatus) {
            List<StatusData> activeStatuses = Arrays.asList(StatusData.ACTIVE, StatusData.INACTIVE);

            if (hasSearch) {
                log.info("Searching users by search='{}' AND statuses {}", search, activeStatuses);
                userPage = userRepository
                        .searchByMultipleFieldsAndStatuses(search, activeStatuses, pageable);
            } else {
                log.info("Fetching users with statuses {}", activeStatuses);
                userPage = userRepository.findByStatusIn(activeStatuses, pageable);
            }
        } else {
            if (hasSearch) {
                log.info("Searching users by search='{}' AND status={}", search, status);
                userPage = userRepository
                        .searchByMultipleFieldsAndStatus(search, status, pageable);
            } else {
                log.info("Searching users by status={}", status);
                userPage = userRepository.findByStatus(status, pageable);
            }
        }

        List<UserResponseDto> content = userPage.getContent().stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());

        log.debug("Retrieved {} users from page {} of {}",
                userPage.getNumberOfElements(),
                userPage.getNumber(), userPage.getTotalPages());

        return userMapper.mapToListDto(content, userPage);
    }



    @Override
    public UserResponseDto getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new NotFoundException("User id " + id + " could not be found");
                });
        log.debug("Retrieved user: {}", user.getUsername());
        return userMapper.mapToDto(user);
    }

    @Override
    public UserResponseDto getUserByToken() {
        log.info("Getting current user by token");
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.debug("Retrieved current user: {}", currentUser.getUsername());
        return userMapper.mapToDto(currentUser);
    }

    @Transactional
    @Override
    public UserResponseDto deleteUserId(Long id) {
        log.info("Deleting user with id: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found for deletion", id);
                    return new NotFoundException("User id " + id + " could not be found");
                });

        log.debug("Clearing roles for user: {}", user.getUsername());
        user.getRoles().clear();
        userRepository.deleteById(id);
        log.info("Successfully deleted user with id: {}", id);
        return userMapper.mapToDto(user);
    }

    @Override
    public UserResponseDto updateUserId(Long id, UpdateUserRequestDto request) {
        log.info("Updating user with id: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found for update", id);
                    return new NotFoundException("User id " + id + " not found");
                });

        // Only update card id if provided in the request
        if (request.getIdCard() != null) {
            // Check if card id is being changed and is already in use by another user
            if (!user.getUsername().equals(request.getIdCard()) &&
                    userRepository.existsByUsername(request.getIdCard())) {
                log.warn("Update failed: Id card already in use: {}", request.getIdCard());
                throw new DuplicateNameException("Id card is already in use, please choose another one.");
            }

            String oldUsername = user.getUsername();
            user.setUsername(request.getIdCard());
            log.info("Updated username: {} -> {}", oldUsername, request.getIdCard());
        }

        // Only update status if provided in the request
        if (request.getStatus() != null) {
            StatusData oldStatus = user.getStatus();
            user.setStatus(request.getStatus());
            log.info("Updated status: {} -> {}", oldStatus, request.getStatus());
        }

        if (request.getProfileUrl() != null) user.setProfileUrl(request.getProfileUrl());

        UserEntity updated = userRepository.save(user);
        log.info("Successfully updated user with id: {}", id);
        return userMapper.mapToDto(updated);
    }

    @Override
    public UserResponseDto changePassword(ChangePasswordRequestDto requestDto) {
        UserEntity user = securityUtils.getCurrentUser();
        log.info("Changing password for current user: {}", user.getUsername());

        // Verify the current password
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed for user {}: Current password is incorrect", user.getUsername());
            throw new BadRequestException("Current password is incorrect.");
        }

        // Optionally, verify that new password and confirm password match
        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("Password change failed for user {}: New password and confirm password do not match", user.getUsername());
            throw new BadRequestException("New password and confirm password do not match.");
        }

        // Update the user's password
        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        UserEntity userEntity = userRepository.save(user);
        log.info("Password successfully changed for user: {}", user.getUsername());
        return userMapper.mapToDto(userEntity);
    }

    @Override
    public UserResponseDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        log.info("Admin changing password for user with id: {}", requestDto.getId());
        UserEntity user = userRepository.findById(requestDto.getId())
                .orElseThrow(() -> {
                    log.error("User with id {} not found for password change", requestDto.getId());
                    return new NotFoundException("User with id card " + requestDto.getId() + " not found");
                });

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("Admin password change failed for user {}: New password and confirm password do not match", user.getUsername());
            throw new BadRequestException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        UserEntity userEntity = userRepository.save(user);
        log.info("Admin successfully changed password for user: {}", user.getUsername());
        return userMapper.mapToDto(userEntity);
    }
}