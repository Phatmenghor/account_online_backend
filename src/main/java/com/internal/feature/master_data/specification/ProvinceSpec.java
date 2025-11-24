package com.internal.feature.master_data.specification;

import com.internal.enumation.StatusData;
import com.internal.feature.master_data.models.Province;
import org.springframework.data.jpa.domain.Specification;

public class ProvinceSpec {

    public static Specification<Province> hasStatus(StatusData status) {
        // Province has no status field, so we ignore this filter
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Province> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("provinceEn")), likePattern),
                    cb.like(cb.lower(root.get("provinceKh")), likePattern),
                    cb.like(cb.lower(root.get("provinceCode")), likePattern)
            );
        };
    }
}
