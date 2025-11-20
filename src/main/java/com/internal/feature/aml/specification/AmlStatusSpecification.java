package com.internal.feature.aml.specification;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.model.AmlStatus;
import org.springframework.data.jpa.domain.Specification;

public class AmlStatusSpecification {

    public static Specification<AmlStatus> hasStatus(AmlStatusEnum status) {
        return (root, query, cb) ->
                status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<AmlStatus> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isEmpty()) return null;

            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("changedBy")), pattern),
                    cb.like(cb.lower(root.get("familyName")), pattern),
                    cb.like(cb.lower(root.get("givenName")), pattern),
                    cb.like(cb.lower(root.get("lastNameKh")), pattern),
                    cb.like(cb.lower(root.get("firstNameKh")), pattern),
                    cb.like(cb.lower(root.get("phoneNumber")), pattern),
                    cb.like(cb.lower(root.get("legalId")), pattern)
            );
        };
    }
}
