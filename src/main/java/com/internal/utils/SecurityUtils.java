package com.internal.utils;

import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.repository.UserRepository;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User not authenticated");
            throw new NotFoundException("User not authenticated.");
        }

        String username = authentication.getName();
        log.debug("Fetching user: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new NotFoundException("User with id card " + username + " not found");
                });
    }
}
