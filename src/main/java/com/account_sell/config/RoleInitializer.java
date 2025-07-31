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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Initializes default roles and admin user on application startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing default roles and admin user...");

        // Create each role if it doesn't already exist
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

        log.info("Role and admin initialization completed");
    }
}