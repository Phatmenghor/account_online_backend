package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.CifActivationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CifActivationLogRepository extends JpaRepository<CifActivationLog, UUID> {

    List<CifActivationLog> findByCifNoOrderByCreatedAtDesc(String cifNo);

    List<CifActivationLog> findByIsSuccessOrderByCreatedAtDesc(Boolean isSuccess);

    List<CifActivationLog> findTop100ByOrderByCreatedAtDesc();
}
