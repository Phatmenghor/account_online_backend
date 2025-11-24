package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {
    Optional<Branch> findByBranchCode(String branchCode);
    boolean existsByBranchCode(String branchCode);
}
