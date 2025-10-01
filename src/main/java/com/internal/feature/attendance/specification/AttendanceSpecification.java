package com.internal.feature.attendance.specification;

import com.internal.enumation.AttendanceStatus;
import com.internal.enumation.AttendanceType;
import com.internal.feature.attendance.models.AttendanceEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSpecification {

    public static Specification<AttendanceEntity> withFilters(
            Long userId,
            AttendanceStatus status,
            AttendanceType type,
            LocalDate startDate,
            LocalDate endDate,
            String search) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("username")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("fullName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("reason")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
