package com.internal.feature.report_staging.specification;

import com.internal.feature.report_staging.models.CbcMainRecordEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CbcMainRecordSpecification {

    public static Specification<CbcMainRecordEntity> withFilters(
            LocalDate startDate,
            LocalDate endDate,
            String search) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Date range filter
            if (startDate != null && endDate != null) {
                Predicate dateRange = criteriaBuilder.or(
                    criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("requestStartDate"), startDate),
                        criteriaBuilder.lessThanOrEqualTo(root.get("requestStartDate"), endDate)
                    ),
                    criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("requestEndDate"), startDate),
                        criteriaBuilder.lessThanOrEqualTo(root.get("requestEndDate"), endDate)
                    )
                );
                predicates.add(dateRange);
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("requestStartDate"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("requestEndDate"), endDate));
            }

            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("accountNumber")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("creditorId")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("accountType")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("asOfDate")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CbcMainRecordEntity> hasAccountNumber(String accountNumber) {
        return (root, query, criteriaBuilder) -> {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("accountNumber"), accountNumber);
        };
    }

    public static Specification<CbcMainRecordEntity> hasCreditorId(String creditorId) {
        return (root, query, criteriaBuilder) -> {
            if (creditorId == null || creditorId.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("creditorId"), creditorId);
        };
    }

    public static Specification<CbcMainRecordEntity> hasProcessingDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("processingDate"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("processingDate"), startDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("processingDate"), endDate);
            }
        };
    }

    public static Specification<CbcMainRecordEntity> orderByCreatedAt() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return criteriaBuilder.conjunction();
        };
    }
}