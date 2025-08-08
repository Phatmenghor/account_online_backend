package com.internal.utils;

import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.repository.UserRepository;
import com.internal.exceptions.error.NotFoundException;
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

        // Get the (username = Idcard) from the authentication object
        String username = authentication.getName();
        log.info("Fetching user with id card: {}", username);

        // Fetch the user from the repository
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User with id card {} not found", username);
                    return new NotFoundException("User with id card " + username + " not found");
            });

        log.info("User with id card {} successfully retrieved", username);
        return user;
    }
}
