
package com.internal.feature.report_staging.specification;

import com.internal.feature.report_staging.models.CbcFinalRecordEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CbcFinalRecordSpecification {

    public static Specification<CbcFinalRecordEntity> withFilters(
            LocalDate startDate,
            LocalDate endDate,
            String batchSessionId,
            String search) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Batch session date range filter
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(
                    root.get("batchSessionDate"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("batchSessionDate"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("batchSessionDate"), endDate));
            }

            // Batch session ID filter
            if (batchSessionId != null && !batchSessionId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("batchSessionId"), batchSessionId.trim()));
            }

            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("accountNumber")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("creditorId")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstNameEnglish")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("familyNameEnglish")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstNameKhmer")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("familyNameKhmer")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("batchSessionId")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("unformattedNameEnglish")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("unformattedNameKhmer")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CbcFinalRecordEntity> hasBatchSessionId(String batchSessionId) {
        return (root, query, criteriaBuilder) -> {
            if (batchSessionId == null || batchSessionId.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("batchSessionId"), batchSessionId);
        };
    }

    public static Specification<CbcFinalRecordEntity> hasBatchSessionDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("batchSessionDate"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("batchSessionDate"), startDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("batchSessionDate"), endDate);
            }
        };
    }

    public static Specification<CbcFinalRecordEntity> wasUpdated(Boolean wasUpdated) {
        return (root, query, criteriaBuilder) -> {
            if (wasUpdated == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("wasUpdated"), wasUpdated);
        };
    }

    public static Specification<CbcFinalRecordEntity> hasAccountNumber(String accountNumber) {
        return (root, query, criteriaBuilder) -> {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("accountNumber"), accountNumber);
        };
    }
}