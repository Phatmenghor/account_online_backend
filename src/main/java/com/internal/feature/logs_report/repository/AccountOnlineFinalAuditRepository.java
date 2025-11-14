package com.internal.feature.logs_report.repository;


import com.internal.feature.logs_report.model.AccountOnlineOpenFinalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface AccountOnlineFinalAuditRepository extends JpaRepository<AccountOnlineOpenFinalAudit,UUID>,
        JpaSpecificationExecutor<AccountOnlineOpenFinalAudit> {

    }
