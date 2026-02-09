package com.internal.feature.master_data.repository;

import com.internal.feature.master_data.models.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByBranchCode(String branchCode);
    boolean existsByBranchCode(String branchCode);

    @Query("SELECT b FROM Branch b WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(b.branchKh) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.branchCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Branch> findBySearch(@Param("search") String search, Pageable pageable);
}
