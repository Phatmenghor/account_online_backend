package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface BranchRepository extends JpaRepository<Branch, String>, JpaSpecificationExecutor<Branch> {
}
