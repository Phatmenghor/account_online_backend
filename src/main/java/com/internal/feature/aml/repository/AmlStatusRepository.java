package com.internal.feature.aml.repository;

import com.internal.feature.aml.model.AmlStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AmlStatusRepository extends JpaRepository<AmlStatus,Long>,
        JpaSpecificationExecutor<AmlStatus> {
}
