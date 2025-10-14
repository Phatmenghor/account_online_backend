package com.internal.feature.auth.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String message = "Your session has expired. Please log in again to continue";
        String status = "failed";
        if (authException instanceof AuthenticationCredentialsNotFoundException) {
            message = authException.getMessage();
        }

        // Send custom response body
        String jsonResponse = "{\"code\": 401, \"status\":\"" + status + "\", \"message\": \"" + message + "\"}";
        response.getWriter().write(jsonResponse);
    }
}
