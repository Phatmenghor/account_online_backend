package com.internal.feature.logs_report.specification;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountOnlineReportLogSpecification {

    public static Specification<AccountOnlineReportLog> filterByDateRangeAndStatus(
            LocalDate fromDate,
            LocalDate toDate,
            List<OpenAccStatusEnum> statuses) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Date range filter - from date
            if (fromDate != null) {
                LocalDateTime fromDateTime = fromDate.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }

            // Date range filter - to date
            if (toDate != null) {
                LocalDateTime toDateTime = toDate.plusDays(1).atStartOfDay();
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), toDateTime));
            }

            // Status filter
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<AccountOnlineReportLog> filter(AccountOnlineReportLogDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dto.getFromDate() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                dto.getFromDate().atStartOfDay()
                        )
                );
            }

            if (dto.getToDate() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                dto.getToDate().atTime(23, 59, 59)
                        )
                );
            }

            if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                predicates.add(root.get("status").in(dto.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
