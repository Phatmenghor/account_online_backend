package com.internal.feature.auth.service;

import com.internal.feature.auth.models.RefreshToken;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.repository.RefreshTokenRepository;
import com.internal.feature.auth.repository.UserRepository;
import com.internal.exceptions.error.custom.NotFoundException;
import com.internal.exceptions.error.custom.TokenRefreshException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration-days:7}")
    private Long refreshTokenDurationDays;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = new RefreshToken();

        UserEntity user = userRepository.findByUsername(username)
                 .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // Delete any existing refresh token for this user to enforce single session per user policy (optional)
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationDays * 24 * 60 * 60)); // days to seconds
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUser(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
