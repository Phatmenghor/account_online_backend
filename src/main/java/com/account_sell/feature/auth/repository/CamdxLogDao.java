package com.account_sell.feature.auth.repository;

import com.account_sell.enumation.CamdxLogEnum;
import com.account_sell.feature.auth.dto.request.RequestLogReport;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CamdxLogDao {

    private final JdbcTemplate jdbcTemplate;

    public List<RequestLogReport> getLogReportPaginated(String status, String appName, LocalDate startDate, LocalDate endDate, int offset, int limit) {
        String sql =
                "WITH input_filter AS (" +
                        "  SELECT ? AS input_status, ? AS input_app_name, ?::date AS start_date, ?::date AS end_date" +
                        ") " +
                        "SELECT request_date::date AS request_date, request_type, status, COUNT(*) AS request_count " +
                        "FROM camdx_log, input_filter " +
                        "WHERE request_type <> 'OCR' " +
                        "  AND status = input_filter.input_status " +
                        "  AND request_date::date BETWEEN input_filter.start_date AND input_filter.end_date " +
                        "  AND (input_filter.input_app_name IS NULL OR application_name = input_filter.input_app_name) " +
                        "GROUP BY request_date::date, request_type, status " +
                        "ORDER BY request_date::date DESC " +
                        "LIMIT ? OFFSET ?";

        return jdbcTemplate.query(
                sql,
                new Object[]{status, appName, startDate, endDate, limit, offset},
                new RequestLogRowMapper()
        );
    }


    // count all data
    public long countLogReport(String status, String appName, LocalDate startDate, LocalDate endDate) {
        String sql =
                "WITH input_filter AS (" +
                        "  SELECT ? AS input_status, ? AS input_app_name, ?::date AS start_date, ?::date AS end_date" +
                        ") " +
                        "SELECT COUNT(*) FROM (" +
                        "  SELECT 1 " +
                        "  FROM camdx_log, input_filter " +
                        "  WHERE request_type <> 'OCR' " +
                        "    AND status = input_filter.input_status " +
                        "    AND request_date::date BETWEEN input_filter.start_date AND input_filter.end_date " +
                        "    AND (input_filter.input_app_name IS NULL OR application_name = input_filter.input_app_name) " +
                        "  GROUP BY request_date::date, request_type, status" +
                        ") AS grouped";

        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{status, appName, startDate, endDate},
                Long.class
        );
    }


    private static class RequestLogRowMapper implements RowMapper<RequestLogReport> {
        @Override
        public RequestLogReport mapRow(ResultSet rs, int rowNum) throws SQLException {
            RequestLogReport report = new RequestLogReport();
            report.setRequestDate(rs.getString("request_date"));
            report.setRequestType(rs.getString("request_type"));
            report.setStatus(CamdxLogEnum.valueOf(rs.getString("status")));
            report.setRequestCount(rs.getInt("request_count"));
            return report;
        }
    }

    // For export excel data
    public List<RequestLogReport> getLogReport(String status, String appName, LocalDate startDate, LocalDate endDate) {
        String sql =
                "WITH input_filter AS (\n" +
                        "  SELECT ? AS input_status, ? AS input_app_name, ?::date AS start_date, ?::date AS end_date\n" +
                        ")\n" +
                        "SELECT request_date::date AS request_date, request_type, status, COUNT(*) AS request_count\n" +
                        "FROM camdx_log, input_filter\n" +
                        "WHERE request_type <> 'OCR'\n" +
                        "  AND status = input_filter.input_status\n" +
                        "  AND request_date::date BETWEEN input_filter.start_date AND input_filter.end_date\n" +
                        "  AND (input_filter.input_app_name IS NULL OR application_name = input_filter.input_app_name)\n" +
                        "GROUP BY request_date::date, request_type, status\n" +
                        "ORDER BY request_date::date ASC";

        return jdbcTemplate.query(sql, new Object[]{status, appName, startDate, endDate}, new RequestLogRowMapper());
    }

    // Get register name app
    public List<String> getAppNames() {
        String sql = "SELECT app_name FROM public.camdx_register ORDER BY id ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
