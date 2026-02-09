package com.internal.feature.master_data.specification;

import com.internal.feature.master_data.models.Commune;
import org.springframework.data.jpa.domain.Specification;

public class CommuneSpec {

    private CommuneSpec() {}

    public static Specification<Commune> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("communeCode")), pattern),
                    cb.like(cb.lower(root.get("communeKh")), pattern)
            );
        };
    }
}
