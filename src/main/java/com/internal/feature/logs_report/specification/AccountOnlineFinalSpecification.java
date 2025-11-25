package com.internal.feature.logs_report.specification;

import com.internal.feature.logs_report.model.AccountOnlineFinal;
import org.springframework.data.jpa.domain.Specification;

public class AccountOnlineFinalSpecification {

    public static Specification<AccountOnlineFinal> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("cif")), likePattern),
                    cb.like(cb.lower(root.get("legalId")), likePattern)
            );
        };
    }
}
