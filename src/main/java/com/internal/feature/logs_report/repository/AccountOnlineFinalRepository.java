package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.AccountOnlineFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AccountOnlineFinalRepository extends JpaRepository<AccountOnlineFinal, UUID>,
        JpaSpecificationExecutor<AccountOnlineFinal> {

    Optional<AccountOnlineFinal> findByLegalId(String legalId);
}
