//package com.internal.feature.project.specification;
//
//import com.internal.feature.project.models.TraineeReport;
//import org.springframework.data.jpa.domain.Specification;
//
//import javax.persistence.criteria.Predicate;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ApplicationSpecification {
//
//    public static Specification<TraineeReport> createSpecification(String search) {
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (search != null && !search.trim().isEmpty()) {
//                String searchTerm = "%" + search.toLowerCase() + "%";
//
//                List<Predicate> searchPredicates = new ArrayList<>();
//
//                // Search across all fields
//                searchPredicates.add(criteriaBuilder.like(
//                    criteriaBuilder.lower(root.get("projectName")), searchTerm));
//
//                searchPredicates.add(criteriaBuilder.like(
//                    criteriaBuilder.lower(root.get("type")), searchTerm));
//
//                searchPredicates.add(criteriaBuilder.like(
//                    criteriaBuilder.lower(root.get("hostServer")), searchTerm));
//
//                searchPredicates.add(criteriaBuilder.like(
//                    criteriaBuilder.lower(root.get("dbName")), searchTerm));
//
//                searchPredicates.add(criteriaBuilder.like(
//                    criteriaBuilder.lower(root.get("dbType")), searchTerm));
//
//                searchPredicates.add(criteriaBuilder.like(
//                    criteriaBuilder.lower(root.get("dbServer")), searchTerm));
//
//                // Handle remark field (can be null)
//                searchPredicates.add(criteriaBuilder.like(
//                    criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("remark"), "")), searchTerm));
//
//                // Handle hostPort field (integer search)
//                try {
//                    Integer portSearch = Integer.parseInt(search);
//                    searchPredicates.add(criteriaBuilder.equal(root.get("hostPort"), portSearch));
//                } catch (NumberFormatException e) {
//                    // Ignore if search term is not a valid integer
//                }
//
//                // Combine all search predicates with OR
//                predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//}

package com.internal.feature.all_application.specification;

import com.internal.enumation.ApplicationStatusEnum;
import com.internal.enumation.ProjectStatusEnum;
import com.internal.feature.all_application.models.Application;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> createSpecification(String search, ApplicationStatusEnum applicationStatus) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";

                List<Predicate> searchPredicates = new ArrayList<>();

                // Search across all fields
                searchPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("projectName")), searchTerm));


                // Combine all search predicates with OR
                predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
            }

            // TraineeReport status filter
            if (applicationStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("applicationStatus"), applicationStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Keep the old method for backward compatibility
    public static Specification<Application> createSpecification(String search) {
        return createSpecification(search, null);
    }
}