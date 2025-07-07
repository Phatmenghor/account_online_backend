package com.account_sell.config;

import com.account_sell.enumation.RoleEnum;
import com.account_sell.enumation.StatusData;
import com.account_sell.feature.auth.models.Role;
import com.account_sell.feature.auth.models.UserEntity;
import com.account_sell.feature.auth.repository.RoleRepository;
import com.account_sell.feature.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes default roles and users on application startup.
 * This runs when the application starts and ensures required data exists.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUserInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.idCard:8888}")
    private String adminCard;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.default-users.create:true}")
    private boolean createDefaultUsers;

    @Value("${app.super.idCard:9999}")
    private String superCard;

    @Value("${app.super.password:super123}")
    private String superPassword;

    @Value("${app.user.idCard:7777}")
    private String userCard;

    @Value("${app.user.password:user123}")
    private String userPassword;

    // -------------------

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.super.email:super@example.com}")
    private String superEmail;

    @Value("${app.user.email:user@example.com}")
    private String userEmail;

    @Override
    public void run(String... args) {
        log.info("Initializing default roles and users...");

        // Create each role if it doesn't already exist
        initializeRoles();

        // Only create default users if enabled in config
        if (createDefaultUsers) {
            // Create default users if they don't exist
            createDefaultAdminUser();
            createDefaultSuperAdminUser();
            createDefaultRegularUser();
        } else {
            log.info("Default user creation is disabled");
        }

        log.info("Role and user initialization completed");
    }

    /**
     * Initialize all required roles
     */
    private void initializeRoles() {
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = new Role();
                role.setName(roleEnum);
                roleRepository.save(role);
                log.info("Created role: {}", roleEnum);
            } else {
                log.debug("Role already exists: {}", roleEnum);
            }
        });
    }

    /**
     * Creates a default admin user if no admin exists.
     */
    private void createDefaultAdminUser() {
        // Skip if user already exists
        if (userRepository.existsByUsername(adminCard)) {
            log.info("Admin user already exists: {}", adminCard);
            return;
        }

        createUserWithRole(adminCard, adminEmail, adminPassword, RoleEnum.ADMIN);
    }

    /**
     * Creates a default super admin user if none exists.
     */
    private void createDefaultSuperAdminUser() {
        // Skip if user already exists
        if (userRepository.existsByUsername(superCard)) {
            log.info("Super admin user already exists: {}", superCard);
            return;
        }

        createUserWithRole(superCard, superEmail, superPassword, RoleEnum.SUPER);
    }

    /**
     * Creates a default regular user if none exists.
     */
    private void createDefaultRegularUser() {
        // Skip if user already exists
        if (userRepository.existsByUsername(userCard)) {
            log.info("Regular user already exists: {}", userCard);
            return;
        }

        createUserWithRole(userCard, userEmail, userPassword, RoleEnum.USER);
    }

    /**
     * Helper method to create a user with a specific role
     */
    private void createUserWithRole(String username, String email, String password, RoleEnum roleEnum) {
        try {
            // Get the role directly from the database
            Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> {
                        log.error("{} role not found, cannot create user", roleEnum);
                        return new RuntimeException(roleEnum + " role not found");
                    });

            // Create new user - IMPORTANT: Initialize with new ArrayList
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setEmail(email);
            user.setStatus(StatusData.ACTIVE);
            user.setPassword(passwordEncoder.encode(password));

            // Create the roles list and add the role
            List<Role> roles = new ArrayList<>();
            roles.add(role);
            user.setRoles(roles);

            // Save the user with the role directly
            userRepository.save(user);

            log.info("Created user: {} with role: {}", username, roleEnum);
        } catch (Exception e) {
            log.error("Error creating user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
}