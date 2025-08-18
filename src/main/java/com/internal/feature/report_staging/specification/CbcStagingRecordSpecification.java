
package com.internal.feature.report_staging.specification;

import com.internal.feature.report_staging.models.CbcStagingRecordEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CbcStagingRecordSpecification {

    public static Specification<CbcStagingRecordEntity> withFilters(String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search filter across multiple fields
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("accountNumber")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("creditorId")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstNameEnglish")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("familyNameEnglish")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstNameKhmer")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("familyNameKhmer")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("unformattedNameEnglish")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("unformattedNameKhmer")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("idNumber1")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("idNumber2")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("idNumber3")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CbcStagingRecordEntity> hasValidationStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("validationStatus"), status);
        };
    }

    public static Specification<CbcStagingRecordEntity> isUpdated(Boolean isUpdated) {
        return (root, query, criteriaBuilder) -> {
            if (isUpdated == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isUpdated"), isUpdated);
        };
    }

    public static Specification<CbcStagingRecordEntity> hasAccountNumber(String accountNumber) {
        return (root, query, criteriaBuilder) -> {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("accountNumber"), accountNumber);
        };
    }

    public static Specification<CbcStagingRecordEntity> hasCreditorId(String creditorId) {
        return (root, query, criteriaBuilder) -> {
            if (creditorId == null || creditorId.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("creditorId"), creditorId);
        };
    }
}