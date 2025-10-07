package com.internal.feature.report_trainee.specification;

import com.internal.feature.report_trainee.models.TraineeReport;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ReportTraineeSpecification {

    public static Specification<TraineeReport> createSpecification(String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                // Also search createdBy and updatedBy from BaseEntity (these are String fields)
                searchPredicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("createdBy")),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("createdBy")),
                                        searchTerm
                                )
                        )
                );

                searchPredicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("updatedBy")),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("updatedBy")),
                                        searchTerm
                                )
                        )
                );

                predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}