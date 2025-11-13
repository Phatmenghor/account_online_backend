package com.internal.feature.logs_report.repository;

import com.internal.feature.logs_report.model.AccountOnlineFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountOnlineFinalRepository extends JpaRepository<AccountOnlineFinal, UUID>,
        JpaSpecificationExecutor<AccountOnlineFinal> {

    @Query(
            value = "SELECT * FROM public.acc_online_open_final " +
                    "WHERE (cif = :cif OR legal_id = :legalId) " +
                    "ORDER BY created_at DESC " +
                    "LIMIT 1",
            nativeQuery = true
    )
    AccountOnlineFinal findAccountByCifOrLegalId(
            @Param("cif") String cif,
            @Param("legalId") String legalId
    );

    Optional<AccountOnlineFinal> findTopByCifOrLegalIdOrderByCreatedAtDesc(String cif, String legalId);

}
