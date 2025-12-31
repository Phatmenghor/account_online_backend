package com.internal.feature.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced JWT Generator with improved role handling and token management.
 */
@Component
@Slf4j
public class JWTGenerator {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration-min}")
    private long jwtExpirationInMinutes;

    @Value("${jwt.issuer:cbc-sender-api}")
    private String issuer;

    private Key getSigningKey() {
        return new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    }

    /**
     * Generate JWT token with user roles and additional claims.
     * @param authentication The authenticated user
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Get authorities/roles
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date currentDate = new Date();
        long expirationTimeInMs = jwtExpirationInMinutes * 60 * 1000;
        Date expireDate = new Date(currentDate.getTime() + expirationTimeInMs);

        log.debug("Generating token for user: {} with roles: {}", username, roles);

        return Jwts.builder()
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .setSubject(username)
                .setIssuer(issuer)
                .claim("roles", roles)
                .claim("created", currentDate.getTime())
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract username from JWT token.
     * @param token JWT token
     * @return Username from token
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Extract roles from JWT token.
     * @param token JWT token
     * @return List of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJWT(String token) {
        Claims claims = parseToken(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * Get time remaining until token expiration in seconds.
     * @param token JWT token
     * @return Seconds until expiration, or 0 if expired
     */
    public long getTokenExpirationTime(String token) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();

        long diff = expiration.getTime() - now.getTime();
        return Math.max(0, diff / 1000); // Convert to seconds, minimum 0
    }

    /**
     * Validate a JWT token.
     * @param token JWT token
     * @return true if valid, exception if invalid
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Parse JWT token and extract claims.
     * @param token JWT token
     * @return Claims from token
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
