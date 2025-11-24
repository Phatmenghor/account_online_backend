package com.internal.feature.master_data.specification;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.Branch;
import org.springframework.data.jpa.domain.Specification;

public class BranchSpec {

    public static Specification<Branch> hasStatus(StatusData status) {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Branch> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("branchKh")), likePattern),
                    cb.like(cb.lower(root.get("branchCode")), likePattern)
            );
        };
    }
}
