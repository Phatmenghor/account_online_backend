package com.internal.feature.auth.service.impl;

import com.internal.enumation.StatusData;
import com.internal.exceptions.error.custom.BadRequestException;
import com.internal.exceptions.error.custom.DuplicateNameException;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.internal.feature.auth.dto.request.ChangePasswordRequestDto;
import com.internal.feature.auth.dto.request.GetAllUserRequestDto;
import com.internal.feature.auth.dto.request.UpdateUserRequestDto;
import com.internal.feature.auth.dto.response.AllUserResponseDto;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.repository.UserRepository;
import com.internal.feature.auth.service.UserService;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.internal.enumation.RoleEnum;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public AllUserResponseDto getAllUser(GetAllUserRequestDto requestDto) {
        log.debug("Getting users with pageNo={}, pageSize={}, search={}, status={}",
                requestDto.getPageNo(), requestDto.getPageSize(), requestDto.getSearch(), requestDto.getStatus());

        GetAllUserRequestDto userRequestDto = new GetAllUserRequestDto(Math.max(requestDto.getPageNo() - 1, 0),
                Math.max(requestDto.getPageSize(), 1),
                requestDto.getSearch(),
                requestDto.getStatus(),
                requestDto.getRoles());

        Pageable pageable = PageRequest.of(userRequestDto.getPageNo(), userRequestDto.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserEntity> userPage = fetchUsers(userRequestDto.getSearch(), userRequestDto.getStatus(), userRequestDto.getRoles(), pageable);

        List<UserResponseDto> content = userPage.getContent().stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());

        return userMapper.mapToListDto(content, userPage);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        log.debug("Getting user by id: {}", id);
        
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));
        
        return userMapper.mapToDto(user);
    }

    @Override
    public UserResponseDto getUserByToken() {
        log.debug("Getting current user by token");
        
        UserEntity currentUser = securityUtils.getCurrentUser();
        return userMapper.mapToDto(currentUser);
    }

    @Transactional
    @Override
    public UserResponseDto deleteUserId(Long id) {
        log.info("Deleting user with id: {}", id);
        
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " could not be found"));

        user.getRoles().clear();
        userRepository.deleteById(id);
        
        return userMapper.mapToDto(user);
    }

    @Override
    public UserResponseDto updateUserId(Long id, UpdateUserRequestDto request) {
        log.debug("Updating user with id: {}", id);
        
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User id " + id + " not found"));

        updateUserFields(user, request);
        UserEntity updated = userRepository.save(user);
        
        return userMapper.mapToDto(updated);
    }

    @Override
    public UserResponseDto changePassword(ChangePasswordRequestDto requestDto) {
        UserEntity user = securityUtils.getCurrentUser();
        log.info("Changing password for current user: {}", user.getUsername());

        validateCurrentPassword(requestDto.getCurrentPassword(), user.getPassword());
        validatePasswordMatch(requestDto.getNewPassword(), requestDto.getConfirmNewPassword());

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        UserEntity userEntity = userRepository.save(user);
        
        return userMapper.mapToDto(userEntity);
    }

    @Override
    public UserResponseDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        log.info("Admin changing password for user with id: {}", requestDto.getId());
        
        UserEntity user = userRepository.findById(requestDto.getId())
                .orElseThrow(() -> new NotFoundException("User with id " + requestDto.getId() + " not found"));

        validatePasswordMatch(requestDto.getNewPassword(), requestDto.getConfirmNewPassword());

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        UserEntity userEntity = userRepository.save(user);
        
        return userMapper.mapToDto(userEntity);
    }

    private Page<UserEntity> fetchUsers(String search, StatusData status, List<String> roles, Pageable pageable) {
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasStatus = status != null;
        boolean hasRoles = roles != null && !roles.isEmpty();

        List<StatusData> statuses = hasStatus ? Collections.singletonList(status) : Arrays.asList(StatusData.ACTIVE, StatusData.DELETE);

        if (hasRoles) {
            List<RoleEnum> roleEnums = roles.stream()
                    .map(role -> {
                        try {
                            return RoleEnum.valueOf(role);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid role: {}", role);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (roleEnums.isEmpty()) {
                return Page.empty(pageable);
            }

            return userRepository.searchByMultipleFieldsAndStatusesAndRoles(search, statuses, roleEnums, pageable);
        } else {
            return hasSearch
                    ? userRepository.searchByMultipleFieldsAndStatuses(search, statuses, pageable)
                    : userRepository.findByStatusIn(statuses, pageable);
        }
    }

    private void updateUserFields(UserEntity user, UpdateUserRequestDto request) {
        if (request.getUsername() != null) {
            validateUniqueIdCard(user, request.getUsername());
            user.setUsername(request.getUsername());
        }
        
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if(request.getEmail() != null){
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getProfileUrl() != null) {
            user.setProfileUrl(request.getProfileUrl());
        }

        if(request.getPosition() != null){
            user.setPosition(request.getPosition());
        }
    }

    private void validateUniqueIdCard(UserEntity user, String newIdCard) {
        if (!user.getUsername().equals(newIdCard) && userRepository.existsByUsername(newIdCard)) {
            throw new DuplicateNameException("Id card is already in use, please choose another one.");
        }
    }

    private void validateCurrentPassword(String currentPassword, String storedPassword) {
        if (!passwordEncoder.matches(currentPassword, storedPassword)) {
            throw new BadRequestException("Current password is incorrect.");
        }
    }

    private void validatePasswordMatch(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("New password and confirm password do not match.");
        }
    }
}
