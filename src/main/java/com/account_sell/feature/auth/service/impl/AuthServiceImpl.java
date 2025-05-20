package com.account_sell.feature.auth.service.impl;

import com.account_sell.enumation.RoleEnum;
import com.account_sell.enumation.StatusData;
import com.account_sell.exceptions.error.BadRequestException;
import com.account_sell.exceptions.error.DuplicateNameException;
import com.account_sell.exceptions.error.NotFoundException;
import com.account_sell.exceptions.error.UnauthorizedException;
import com.account_sell.feature.auth.dto.request.LoginRequestDto;
import com.account_sell.feature.auth.dto.request.RegisterRequestDto;
import com.account_sell.feature.auth.dto.response.AuthResponseDTO;
import com.account_sell.feature.auth.dto.response.UserResponseDto;
import com.account_sell.feature.auth.mapper.AuthMapper;
import com.account_sell.feature.auth.models.Role;
import com.account_sell.feature.auth.models.UserEntity;
import com.account_sell.feature.auth.repository.RoleRepository;
import com.account_sell.feature.auth.repository.UserRepository;
import com.account_sell.feature.auth.security.JWTGenerator;
import com.account_sell.feature.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the AuthService interface.
 */
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

    @Override
    public AuthResponseDTO login(LoginRequestDto loginDto) {
        log.info("Processing login request for user: {}", loginDto.getEmail());

        // Check if user exists before authentication
        UserEntity userEntity = userRepository.findByUsername(loginDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email: {}", loginDto.getEmail());
                    return new NotFoundException("User not found");
                });

        // Check if user is active
        if (userEntity.getStatus() != StatusData.ACTIVE) {
            log.warn("Login rejected: User {} is not active. Current status: {}", 
                    loginDto.getEmail(), userEntity.getStatus());
            throw new UnauthorizedException("Account is inactive. Please contact an administrator.");
        }

        // Proceed with authentication
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);

            UserResponseDto userDto = authMapper.userToUserResponseDto(userEntity);

            log.info("User {} logged in successfully", loginDto.getEmail());
            
            return new AuthResponseDTO(token, userDto);
        } catch (Exception e) {
            log.warn("Authentication failed for user {}: {}", loginDto.getEmail(), e.getMessage());
            throw e; // Let the exception handler deal with this
        }
    }

    @Override
    public UserResponseDto register(RegisterRequestDto registerDto) {
        log.info("Processing registration request with email: {}", registerDto.getEmail());
        return createUserInternal(registerDto, "Registration");
    }
    
    @Override
    public UserResponseDto createUserByAdmin(RegisterRequestDto registerDto) {
        log.info("Processing admin user creation with email: {}", registerDto.getEmail());
        return createUserInternal(registerDto, "Admin creation");
    }

    @Override
    public List<Map<String, Object>> getAvailableRoles() {
        log.info("Fetching available roles");

        List<Map<String, Object>> rolesList = Arrays.stream(RoleEnum.values())
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("code", role.name());

                    // Format display name with uppercase first letter for each word
                    String displayName = Arrays.stream(role.name().split("_"))
                            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    roleMap.put("displayName", displayName);
                    return roleMap;
                })
                .collect(Collectors.toList());

        log.info("Retrieved {} available roles", rolesList.size());
        return rolesList;
    }

    @Override
    public boolean validateToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.info("Validating token for user: {}", username);
        
        // Check if user is still active
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getStatus() != StatusData.ACTIVE) {
            log.warn("Token validation failed: User {} is not active", username);
            return false;
        }

        log.info("Token successfully validated for user: {}", username);
        return true;
    }

    private UserResponseDto createUserInternal(RegisterRequestDto registerDto, String operationType) {
        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            log.warn("{} failed: Email already in use: {}", operationType, registerDto.getEmail());
            throw new DuplicateNameException("Email is already in use, please choose another one.");
        }

        // Validate role
        if (registerDto.getRole() == null) {
            log.warn("{} failed: No role provided for user {}", operationType, registerDto.getEmail());
            throw new BadRequestException("Role is required for user creation.");
        }

        // Ensure role exists in the database
        Role role = roleRepository.findByName(registerDto.getRole())
                .orElseThrow(() -> {
                    log.warn("{} failed: Invalid role provided: {} for user {}", 
                            operationType, registerDto.getRole(), registerDto.getEmail());
                    return new BadRequestException("Invalid role provided: " + registerDto.getRole());
                });

        try {
            // Create user
            UserEntity user = new UserEntity();
            user.setUsername(registerDto.getEmail());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setStatus(registerDto.getStatus() != null ? registerDto.getStatus() : StatusData.ACTIVE);
            user.setRoles(Collections.singletonList(role));
            user.setProfileUrl(registerDto.getProfileUrl());

            // Save the user
            final UserEntity savedUser = userRepository.save(user);
            log.info("{} successful: User created with email: {}, status: {}, role: {}", 
                    operationType, registerDto.getEmail(), user.getStatus(), registerDto.getRole());

            return authMapper.userToUserResponseDto(savedUser);
        } catch (Exception e) {
            log.error("{} failed: Error creating user {}: {}", operationType, registerDto.getEmail(), e.getMessage());
            throw e;
        }
    }
}