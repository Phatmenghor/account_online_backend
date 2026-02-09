package com.internal.feature.master_data.specification;

import com.internal.feature.master_data.models.District;
import org.springframework.data.jpa.domain.Specification;

public class DistrictSpec {

    private DistrictSpec() {}

    public static Specification<District> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("districtCode")), pattern),
                    cb.like(cb.lower(root.get("districtKh")), pattern)
            );
        };
    }
}
