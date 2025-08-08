package com.internal.feature.auth.security;

import com.internal.enumation.RoleEnum;
import com.internal.exceptions.error.UnauthorizedException;
import com.internal.feature.auth.models.UserEntity;
import com.internal.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionService {

    private final SecurityUtils securityUtils;

    /**
     * Check if the current user has the specified role.
     * @param role Role to check
     * @return true if user has the role
     */
    public boolean hasRole(RoleEnum role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role.name()));
    }

    /**
     * Check if the current user has any of the specified roles.
     * @param roles Roles to check
     * @return true if user has any of the roles
     */
    public boolean hasAnyRole(RoleEnum... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Set<String> userRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Arrays.stream(roles)
                .anyMatch(role -> userRoles.contains(role.name()));
    }

    /**
     * Check if the current user has all of the specified roles.
     * @param roles Roles to check
     * @return true if user has all of the roles
     */
    public boolean hasAllRoles(RoleEnum... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Set<String> userRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Arrays.stream(roles)
                .allMatch(role -> userRoles.contains(role.name()));
    }

    /**
     * Require the current user to have the specified role.
     * @param role Required role
     * @param message Error message if not authorized
     * @throws UnauthorizedException if user does not have the role
     */
    public void requireRole(RoleEnum role, String message) {
        if (!hasRole(role)) {
            log.warn("Access denied: User does not have required role {}", role);
            throw new UnauthorizedException(message);
        }
    }

    /**
     * Require the current user to have any of the specified roles.
     * @param message Error message if not authorized
     * @param roles Required roles (any)
     * @throws UnauthorizedException if user does not have any of the roles
     */
    public void requireAnyRole(String message, RoleEnum... roles) {
        if (!hasAnyRole(roles)) {
            log.warn("Access denied: User does not have any required roles {}", Arrays.toString(roles));
            throw new UnauthorizedException(message);
        }
    }

    /**
     * Require the current user to have all of the specified roles.
     * @param message Error message if not authorized
     * @param roles Required roles (all)
     * @throws UnauthorizedException if user does not have all of the roles
     */
    public void requireAllRoles(String message, RoleEnum... roles) {
        if (!hasAllRoles(roles)) {
            log.warn("Access denied: User does not have all required roles {}", Arrays.toString(roles));
            throw new UnauthorizedException(message);
        }
    }

    /**
     * Check if the current user is the owner of the resource or has admin role.
     * @param resourceOwnerId The ID of the resource owner
     * @return true if user is owner or admin
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            
            // Check if user is an admin
            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleEnum.ADMIN);
                    
            // Check if user is the owner
            boolean isOwner = currentUser.getId().equals(resourceOwnerId);
            
            return isAdmin || isOwner;
        } catch (Exception e) {
            log.error("Error checking ownership: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Require the current user to be either the resource owner or have admin role.
     * @param resourceOwnerId The ID of the resource owner
     * @param message Error message if not authorized
     * @throws UnauthorizedException if user is not owner or admin
     */
    public void requireOwnerOrAdmin(Long resourceOwnerId, String message) {
        if (!isOwnerOrAdmin(resourceOwnerId)) {
            log.warn("Access denied: User is neither the resource owner nor an admin");
            throw new UnauthorizedException(message);
        }
    }
}