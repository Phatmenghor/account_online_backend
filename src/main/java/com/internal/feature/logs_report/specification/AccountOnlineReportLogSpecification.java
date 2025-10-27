package com.internal.feature.logs_report.specification;

import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class AccountOnlineReportLogSpecification {

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
