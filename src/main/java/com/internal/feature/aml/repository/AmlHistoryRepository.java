package com.internal.feature.aml.repository;

import com.internal.feature.aml.model.AmlHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AmlHistoryRepository extends JpaRepository<AmlHistory, Long>, JpaSpecificationExecutor<AmlHistory> {
}
