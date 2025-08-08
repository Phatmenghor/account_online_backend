package com.internal.feature.auth.security;

import com.internal.config.RequiresRole;
import com.internal.enumation.RoleEnum;
import com.internal.exceptions.error.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect that enforces role-based access control using the RequiresRole annotation.
 * Intercepts method calls and checks if the current user has the required roles.
 */
@Aspect
@Component
@Slf4j
public class RoleSecurityAspect {

    @Around("@annotation(com.internal.config.RequiresRole) || " +
            "@within(com.internal.config.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Checking role permissions for {}", joinPoint.getSignature().toShortString());

        // Get method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get annotation from method first, then from class if not present on method
        RequiresRole annotation = AnnotationUtils.findAnnotation(method, RequiresRole.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequiresRole.class);
        }

        if (annotation != null) {
            // Extract required roles from annotation
            String[] requiredRoles = annotation.value();
            boolean anyRole = annotation.anyRole();
            String customMessage = annotation.message();

            log.debug("Required roles: {}, anyRole: {}", Arrays.toString(requiredRoles), anyRole);

            // Get current authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Access attempt without authentication");
                throw new UnauthorizedException("User not authenticated");
            }

            // Get user's actual roles
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Set<String> userRoles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            log.debug("User roles: {}", userRoles);

            // Convert requiredRoles to RoleEnum format if needed
            Set<String> formattedRequiredRoles = Arrays.stream(requiredRoles)
                    .map(role -> {
                        try {
                            // If the role is specified as enum constant, convert to string
                            return RoleEnum.valueOf(role).name();
                        } catch (IllegalArgumentException e) {
                            // If not a valid enum, use as is (might be already a string)
                            return role;
                        }
                    })
                    .collect(Collectors.toSet());

            boolean hasAccess = false;

            if (anyRole) {
                // Check if user has any of the required roles
                hasAccess = formattedRequiredRoles.stream()
                        .anyMatch(userRoles::contains);

                if (!hasAccess) {
                    log.warn("Access denied: user has none of the required roles");
                    throw new UnauthorizedException(customMessage);
                }
            } else {
                // Check if user has all of the required roles
                hasAccess = userRoles.containsAll(formattedRequiredRoles);

                if (!hasAccess) {
                    log.warn("Access denied: user missing some required roles");
                    throw new UnauthorizedException(customMessage);
                }
            }

            log.debug("Access granted for {}", joinPoint.getSignature().toShortString());
        }

        // If annotation is null or access is granted, proceed with method execution
        return joinPoint.proceed();
    }
}