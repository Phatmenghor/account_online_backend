package com.internal.feature.master_data.specification;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.Village;
import org.springframework.data.jpa.domain.Specification;

public class VillageSpec {

    public static Specification<Village> hasStatus(StatusData status) {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Village> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("villageEn")), likePattern),
                    cb.like(cb.lower(root.get("villageKh")), likePattern),
                    cb.like(cb.lower(root.get("villageCode")), likePattern)
            );
        };
    }
}
