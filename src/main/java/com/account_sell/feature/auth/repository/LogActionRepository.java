package com.account_sell.feature.auth.repository;

import com.account_sell.feature.auth.models.LogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LogActionRepository extends JpaRepository<LogAction, Long> {
}