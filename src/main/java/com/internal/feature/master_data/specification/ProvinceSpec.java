package com.internal.feature.master_data.specification;

import com.internal.feature.master_data.models.Province;
import org.springframework.data.jpa.domain.Specification;

public class ProvinceSpec {

    private ProvinceSpec() {}

    public static Specification<Province> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("provinceCode")), pattern),
                    cb.like(cb.lower(root.get("provinceKh")), pattern)
            );
        };
    }
}
