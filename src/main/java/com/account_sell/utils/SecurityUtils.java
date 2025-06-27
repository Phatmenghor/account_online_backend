package com.account_sell.utils;

import com.account_sell.feature.auth.models.UserEntity;
import com.account_sell.feature.auth.repository.UserRepository;
import com.account_sell.exceptions.error.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityUtils {
    @Autowired
    private UserRepository userRepository;

    public UserEntity getCurrentUser() {
        // Retrieve the Authentication object from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If no authentication exists (e.g., anonymous user), log error and throw exception
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User not authenticated. Authentication object is null or not authenticated.");
            throw new NotFoundException("User not authenticated.");
        }

        // Get the username (email) from the authentication object
        String username = authentication.getName();
        log.info("Fetching user with email: {}", username);

        // Fetch the user from the repository
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User with email {} not found", username);
                    return new NotFoundException("User with email " + username + " not found");
            });

        log.info("User with email {} successfully retrieved", username);
        return user;
    }
}
