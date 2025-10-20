package com.internal.feature.telegram_alerts.repository;

import com.internal.feature.telegram_alerts.model.NidValidationCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NidValidationCacheRepository extends JpaRepository<NidValidationCache, Long> {
    NidValidationCache findTopByOrderByCreatedAtDesc();
}
