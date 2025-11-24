package com.internal.feature.master_data.specification;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.Commune;
import org.springframework.data.jpa.domain.Specification;

public class CommuneSpec {

    public static Specification<Commune> hasStatus(StatusData status) {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Commune> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("communeEn")), likePattern),
                    cb.like(cb.lower(root.get("communeKh")), likePattern),
                    cb.like(cb.lower(root.get("communeCode")), likePattern)
            );
        };
    }
}
