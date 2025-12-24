package com.internal.feature.master_data.specification;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.LegalType;
import org.springframework.data.jpa.domain.Specification;

public class LegalTypeSpec {
    public static Specification<LegalType> hasStatus(StatusData status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<LegalType> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nameEn")), likePattern),
                    cb.like(cb.lower(root.get("nameKh")), likePattern),
                    cb.like(cb.lower(root.get("legalTypeValue")), likePattern)
            );
        };
    }
}
