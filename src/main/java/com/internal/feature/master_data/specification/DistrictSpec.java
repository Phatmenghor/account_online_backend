package com.internal.feature.master_data.specification;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.District;
import org.springframework.data.jpa.domain.Specification;

public class DistrictSpec {

    public static Specification<District> hasStatus(StatusData status) {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<District> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("districtEn")), likePattern),
                    cb.like(cb.lower(root.get("districtKh")), likePattern),
                    cb.like(cb.lower(root.get("districtCode")), likePattern)
            );
        };
    }
}
