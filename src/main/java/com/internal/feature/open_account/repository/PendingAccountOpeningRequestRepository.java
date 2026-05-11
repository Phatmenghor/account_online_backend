package com.internal.feature.open_account.repository;

import com.internal.enumation.AccountOpeningRequestStatusEnum;
import com.internal.feature.open_account.models.PendingAccountOpeningRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingAccountOpeningRequestRepository extends JpaRepository<PendingAccountOpeningRequest, Long> {

    Optional<PendingAccountOpeningRequest> findByLegalIdAndStatus(String legalId, AccountOpeningRequestStatusEnum status);

    List<PendingAccountOpeningRequest> findByLegalIdOrderByCreatedAtDesc(String legalId);

    List<PendingAccountOpeningRequest> findByStatus(AccountOpeningRequestStatusEnum status);

    Optional<PendingAccountOpeningRequest> findFirstByLegalIdOrderByCreatedAtDesc(String legalId);
}
