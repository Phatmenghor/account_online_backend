package com.internal.feature.auth.repository;

import com.internal.feature.auth.models.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {
    Optional<ApiKeyEntity> findByApiKey(String apiKey);
    boolean existsByApiKey(String apiKey);
}
