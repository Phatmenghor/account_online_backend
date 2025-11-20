package com.internal.feature.aml.specification;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.model.AmlHistory;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AmlHistorySpecification {
    public static Specification<AmlHistory> createdBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(
                        root.get("createdAt"),
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                );
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), start.atStartOfDay());
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), end.atTime(23, 59, 59));
            }
            return null;
        };
    }

    public static Specification<AmlHistory> hasStatus(AmlStatusEnum status) {
        return (root, query, cb) ->
                status != null ? cb.equal(root.get("status"), status) : null;
    }

    /** Search in reqPayload and resPayload */
    public static Specification<AmlHistory> search(String keyword) {
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
