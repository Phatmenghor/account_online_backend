package com.internal.feature.master_data.specification;

import com.internal.feature.master_data.models.Village;
import org.springframework.data.jpa.domain.Specification;

public class VillageSpec {

    private VillageSpec() {}

    public static Specification<Village> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("villageCode")), pattern),
                    cb.like(cb.lower(root.get("villageKh")), pattern)
            );
        };
    }
}
