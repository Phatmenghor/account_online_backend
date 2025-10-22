package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.NidValidationFailureLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface NidValidationFailureLogsRepository extends JpaRepository<NidValidationFailureLogs, UUID>, 
        JpaSpecificationExecutor<NidValidationFailureLogs> {
}
