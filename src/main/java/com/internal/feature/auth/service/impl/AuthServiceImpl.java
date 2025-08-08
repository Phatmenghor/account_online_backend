package com.internal.feature.auth.service.impl;

import com.internal.enumation.RoleEnum;
import com.internal.enumation.StatusData;
import com.internal.exceptions.error.BadRequestException;
import com.internal.exceptions.error.DuplicateNameException;
import com.internal.exceptions.error.NotFoundException;
import com.internal.exceptions.error.UnauthorizedException;
import com.internal.feature.auth.dto.request.LoginRequestDto;
import com.internal.feature.auth.dto.request.RegisterRequestDto;
import com.internal.feature.auth.dto.response.AuthResponseDTO;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.mapper.AuthMapper;
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
        log.info("Processing login request for user: {}", loginDto.getIdCard());

        // Check if user exists before authentication
        UserEntity userEntity = userRepository.findByUsername(loginDto.getIdCard())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with id card: {}", loginDto.getIdCard());
                    return new NotFoundException("User not found");
                });

        // Check if user is active
        if (userEntity.getStatus() != StatusData.ACTIVE) {
            log.warn("Login rejected: User {} is not active. Current status: {}", 
                    loginDto.getIdCard(), userEntity.getStatus());
            throw new UnauthorizedException("Account is inactive. Please contact an administrator.");
        }

        // Proceed with authentication
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getIdCard(),
                            loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);

            UserResponseDto userDto = authMapper.userToUserResponseDto(userEntity);

            log.info("User {} logged in successfully", loginDto.getIdCard());
            
            return new AuthResponseDTO(token, userDto);
        } catch (Exception e) {
            log.warn("Authentication failed for user {}: {}", loginDto.getIdCard(), e.getMessage());
            throw e; // Let the exception handler deal with this
        }
    }

    @Override
    public UserResponseDto register(RegisterRequestDto registerDto) {
        log.info("Processing registration request with id card: {}", registerDto.getIdCard());
        return createUserInternalRegister(registerDto, "Registration");
    }
    
    @Override
    public UserResponseDto createUserByAdmin(RegisterRequestDto registerDto) {
        log.info("Processing admin user creation with id card: {}", registerDto.getIdCard());
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

    private UserResponseDto createUserInternalRegister(RegisterRequestDto registerDto, String operationType) {

        // Check if id card is already in use
        if (userRepository.existsByUsername(registerDto.getIdCard())) {
            log.warn("{} failed: Id card already in use: {}", operationType, registerDto.getIdCard());
            throw new DuplicateNameException("Id card is already in use, please choose another one.");
        }

        // Use provided role or default to USER
        String roleName = registerDto.getRole() != null ? String.valueOf(registerDto.getRole()) : RoleEnum.USER.name();

        // Validate and fetch role from DB
        Role role = roleRepository.findByName(RoleEnum.valueOf(roleName.toUpperCase()))
                .orElseThrow(() -> {
                    log.warn("{} failed: Invalid role provided: {} for user {}", operationType, roleName, registerDto.getIdCard());
                    return new BadRequestException("Invalid role provided: " + roleName);
                });

        try {
            // Create user
            UserEntity user = new UserEntity();
            user.setEmail(registerDto.getEmail());
            user.setUsername(registerDto.getIdCard());
            user.setFullName(registerDto.getFullName());
            user.setPosition(registerDto.getPosition());
            user.setBranch(registerDto.getBranch());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setStatus(StatusData.PENDING);
            user.setRoles(Collections.singletonList(role));

            // Save user
            final UserEntity savedUser = userRepository.save(user);
            log.info("{} successful: User created with id card : {}, status: {}, role: {}",
                    operationType, registerDto.getIdCard(), user.getStatus(), role.getName());

            return authMapper.userToUserResponseDto(savedUser);
        } catch (Exception e) {
            log.error("{} failed : Error creating user {}: {}", operationType, registerDto.getIdCard(), e.getMessage());
            throw e;
        }
    }


    private UserResponseDto createUserInternal(RegisterRequestDto registerDto, String operationType) {
        // Check if id card is already in use
        if (userRepository.existsByUsername(registerDto.getIdCard())) {
            log.warn("{} failed: Id card already in use : {}", operationType, registerDto.getIdCard());
            throw new DuplicateNameException("Id card is already in use, please choose another one.");
        }

        // Validate role
        if (registerDto.getRole() == null) {
            log.warn("{} failed: No role provided for user: {}", operationType, registerDto.getIdCard());
            throw new BadRequestException("Role is required for user creation.");
        }

        // Ensure role exists in the database
        Role role = roleRepository.findByName(registerDto.getRole())
                .orElseThrow(() -> {
                    log.warn("{} failed: Invalid role provided : {} for user {}",
                            operationType, registerDto.getRole(), registerDto.getIdCard());
                    return new BadRequestException("Invalid role provided: " + registerDto.getRole());
                });

        try {
            // Create user
            UserEntity user = new UserEntity();
            user.setUsername(registerDto.getIdCard());
            user.setEmail(registerDto.getEmail());
            user.setFullName(registerDto.getFullName());
            user.setPosition(registerDto.getPosition());
            user.setBranch(registerDto.getBranch());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setStatus(StatusData.ACTIVE);
            user.setRoles(Collections.singletonList(role));

            // Save the user
            final UserEntity savedUser = userRepository.save(user);
            log.info("{} successful: User created with id card: {}, status: {}, role: {}",
                    operationType, registerDto.getIdCard(), user.getStatus(), registerDto.getRole());

            return authMapper.userToUserResponseDto(savedUser);
        } catch (Exception e) {
            log.error("{} failed: Error creating user {} : {}", operationType, registerDto.getIdCard(), e.getMessage());
            throw e;
        }
    }
}