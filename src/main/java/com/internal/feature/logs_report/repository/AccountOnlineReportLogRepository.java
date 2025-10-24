package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AccountOnlineReportLogRepository extends JpaRepository<AccountOnlineReportLog, UUID>,
        JpaSpecificationExecutor<AccountOnlineReportLog> {
}
