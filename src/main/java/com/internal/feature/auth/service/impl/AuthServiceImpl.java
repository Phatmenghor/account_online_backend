package com.internal.feature.auth.service.impl;

import com.internal.enumation.RoleEnum;
import com.internal.enumation.StatusData;
import com.internal.exceptions.error.BadRequestException;
import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.exceptions.error.UnauthorizedException;
import com.internal.feature.auth.dto.request.LoginRequestDto;
import com.internal.feature.auth.dto.request.RegisterRequestDto;
import com.internal.feature.auth.dto.request.UpdateUserRequestDto;
import com.internal.feature.auth.dto.response.AuthResponseDTO;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.mapper.AuthMapper;
import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.auth.models.Role;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.repository.RoleRepository;
import com.internal.feature.auth.repository.UserRepository;
import com.internal.feature.auth.security.JWTGenerator;
import com.internal.feature.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;

    @Override
    public AuthResponseDTO login(LoginRequestDto loginDto) {
        log.info("Processing login request for user: {}", loginDto.getIdCard());

        UserEntity userEntity = userRepository.findByUsername(loginDto.getIdCard())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with id card: {}", loginDto.getIdCard());
                    return new NotFoundException("User not found");
                });

        if (userEntity.getStatus() != StatusData.ACTIVE) {
            log.warn("Login rejected: User {} is not active. Current status: {}", 
                    loginDto.getIdCard(), userEntity.getStatus());
            throw new UnauthorizedException("Account is inactive. Please contact an administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getIdCard(),
                        loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        UserResponseDto userDto = authMapper.userToUserResponseDto(userEntity);
        log.info("User {} logged in successfully", loginDto.getIdCard());
        
        return new AuthResponseDTO(token, userDto);
    }

    @Override
    public UserResponseDto register(RegisterRequestDto registerDto) {
        log.info("Processing registration request with id card: {}", registerDto.getIdCard());
        return createUser(registerDto, StatusData.PENDING, "Registration");
    }
    
    @Override
    public UserResponseDto createUserByAdmin(RegisterRequestDto registerDto) {
        log.info("Processing admin user creation with id card: {}", registerDto.getIdCard());
        return createUser(registerDto, StatusData.ACTIVE, "Admin creation");
    }

    @Override
    public UserResponseDto updateUserProfile(UpdateUserRequestDto requestDto, String name) {
        log.info("Processing update user profile: {}", requestDto.getIdCard());

        // 1. Load user by username (e.g. idCard or username)
        UserEntity user = userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Update fields
        updateUserFields(user, requestDto);


        // 3. Save updated user entity
        UserEntity updatedUser = userRepository.save(user);

        return userMapper.mapToDto(updatedUser);
    }

    private void updateUserFields(UserEntity user, UpdateUserRequestDto request) {
        if (request.getIdCard() != null) {
            validateUniqueIdCard(user, request.getIdCard());
            user.setUsername(request.getIdCard());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getProfileUrl() != null) {
            user.setProfileUrl(request.getProfileUrl());
        }

        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }

        if (request.getBranch() != null) {
            user.setBranch(request.getBranch());
        }
    }

    private void validateUniqueIdCard(UserEntity user, String newIdCard) {
        if (!user.getUsername().equals(newIdCard) && userRepository.existsByUsername(newIdCard)) {
            throw new DuplicateNameException("Id card is already in use, please choose another one.");
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableRoles() {
        log.debug("Fetching available roles");

        return Arrays.stream(RoleEnum.values())
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("code", role.name());
                    roleMap.put("displayName", formatDisplayName(role.name()));
                    return roleMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.debug("Validating token for user: {}", username);
        
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getStatus() != StatusData.ACTIVE) {
            log.warn("Token validation failed: User {} is not active", username);
            return false;
        }

        return true;
    }

    private UserResponseDto createUser(RegisterRequestDto registerDto, StatusData status, String operationType) {
        if (userRepository.existsByUsername(registerDto.getIdCard())) {
            log.warn("{} failed: Id card already in use: {}", operationType, registerDto.getIdCard());
            throw new DuplicateNameException("Id card is already in use, please choose another one.");
        }

        Role role = roleRepository.findByName( registerDto.getRole())
                .orElseThrow(() -> {
                    log.warn("{} failed: Invalid role provided: {} for user {}", 
                            operationType,  registerDto.getRole(), registerDto.getIdCard());
                    return new BadRequestException("Invalid role provided: " +  registerDto.getRole());
                });

        UserEntity user = createUserEntity(registerDto, status, role);
        UserEntity savedUser = userRepository.save(user);
        
        log.info("{} successful: User created with id card: {}, status: {}, role: {}",
                operationType, registerDto.getIdCard(), status,  registerDto.getRole());

        return authMapper.userToUserResponseDto(savedUser);
    }

    private UserEntity createUserEntity(RegisterRequestDto registerDto, StatusData status, Role role) {
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getIdCard());
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());
        user.setPosition(registerDto.getPosition());
        user.setBranch(registerDto.getBranch());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setStatus(status);
        user.setRoles(Collections.singletonList(role));
        return user;
    }

    private String formatDisplayName(String roleName) {
        return Arrays.stream(roleName.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}