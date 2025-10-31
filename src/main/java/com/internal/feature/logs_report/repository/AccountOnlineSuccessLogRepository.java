package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import com.internal.feature.logs_report.model.AccountOnlineSuccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AccountOnlineSuccessLogRepository extends JpaRepository<AccountOnlineSuccessLog, UUID>,
        JpaSpecificationExecutor<AccountOnlineReportLog> {
}
