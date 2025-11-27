package com.internal.feature.setting.repository;

import com.internal.feature.setting.models.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
}
