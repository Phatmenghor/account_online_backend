package com.internal.utils.specification.reference;

import com.internal.enumation.StatusData;
import com.internal.feature.reference.models.Occupation;
import org.springframework.data.jpa.domain.Specification;

public class OccupationSpec {

    public static Specification<Occupation> hasStatus(StatusData status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Occupation> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nameEn")), likePattern),
                    cb.like(cb.lower(root.get("nameKh")), likePattern)
            );
        };
    }
}
