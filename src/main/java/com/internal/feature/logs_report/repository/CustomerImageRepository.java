package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.CustomerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerImageRepository extends JpaRepository<CustomerImage, UUID> {
    Optional<CustomerImage> findByTypeAndId(String type, UUID id);
}
