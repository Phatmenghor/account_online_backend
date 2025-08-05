package com.account_sell.feature.auth.service;

import com.account_sell.enumation.CamdxLogEnum;
import com.account_sell.feature.auth.dto.request.RequestLogReport;
import com.account_sell.feature.auth.dto.response.LogRegisterAppDto;
import com.account_sell.feature.auth.dto.response.ResponseLogReport;
import com.account_sell.feature.auth.dto.response.ResponseLogReportExport;
import com.account_sell.feature.auth.dto.response.TotalCountDto;
import com.account_sell.feature.auth.repository.CamdxLogDao;
import com.account_sell.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LogReportService {

    private final CamdxLogDao camdxLogDao;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Get all log data to view
    public PaginationResponse<ResponseLogReport> getReport(
            int pageNo,
            int pageSize,
            CamdxLogEnum status,
            String appName,
            LocalDate startDate,
            LocalDate endDate) {

        // Ensure pageNo starts from 1, then calculate offset
        int safePageNo = Math.max(pageNo, 1); // fallback if pageNo < 1
        int offset = (safePageNo - 1) * pageSize;

        // Fetch paginated data
        List<RequestLogReport> requestLogList = camdxLogDao.getLogReportPaginated(
                status != null ? status.name() : null,
                appName,
                startDate,
                endDate,
                offset,
                pageSize);

        // Count total matching records
        long totalElements = camdxLogDao.countLogReport(
                status != null ? status.name() : null,
                appName,
                startDate,
                endDate);

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // Convert to response
        List<ResponseLogReport> content = requestLogList.stream().map(log -> {
            ResponseLogReport res = new ResponseLogReport();
            res.setRequestDate(log.getRequestDate());
            res.setRequestType(log.getRequestType());
            res.setStatus(log.getStatus()); // You can map from entity instead of using static `status`
            res.setRequestCount(log.getRequestCount());
            return res;
        }).collect(Collectors.toList());

        // ADD THIS: Loop through all pages to sum requestCount
        int totalRequestCountSum = 0;
        for (int page = 1; page <= totalPages; page++) {
            int pageOffset = (page - 1) * pageSize;
            List<RequestLogReport> pageData = camdxLogDao.getLogReportPaginated(
                    status != null ? status.name() : null,
                    appName,
                    startDate,
                    endDate,
                    pageOffset,
                    pageSize);

            totalRequestCountSum += pageData.stream()
                    .mapToInt(RequestLogReport::getRequestCount)
                    .sum();
        }

        System.out.println("Total sum of requestCount for all " + totalElements + " records: " + totalRequestCountSum);

        // Wrap response in PaginatedResponse
        PaginationResponse<ResponseLogReport> response = new PaginationResponse<>();
        response.setContent(content);
        response.setPageNo(safePageNo);
        response.setPageSize(pageSize);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setTotalCount(totalRequestCountSum);

        return response;
    }

    // Get all log data to export excel
    public ResponseLogReportExport getReportExcel(CamdxLogEnum status, String appName, LocalDate startDate, LocalDate endDate) {
        List<RequestLogReport> dbResult = camdxLogDao.getLogReport(status.name(), appName, startDate, endDate);

        List<ResponseLogReport> responseList = new ArrayList<>();
        int totalCount = 0;

        for (RequestLogReport log : dbResult) {
            ResponseLogReport res = new ResponseLogReport();
            res.setRequestDate(log.getRequestDate());
            res.setRequestType(log.getRequestType());
            res.setStatus(log.getStatus());
            res.setRequestCount(log.getRequestCount());

            responseList.add(res);
            totalCount += log.getRequestCount(); // Sum here
        }

        ResponseLogReportExport result = new ResponseLogReportExport();
        result.setContent(responseList);
        result.setTotalRequestCount(totalCount);

        return result;
    }

    public List<TotalCountDto> getCountReportExcel(CamdxLogEnum status, String appName, LocalDate startDate, LocalDate endDate) {
        List<RequestLogReport> dbResult = camdxLogDao.getLogReport(status.name(), appName, startDate, endDate);

        long totalCount = dbResult.size(); // Count number of filtered records

        return Stream.of(totalCount)
                .map(count -> {
                    TotalCountDto dto = new TotalCountDto();
                    dto.setTotalCount(count);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public List<LogRegisterAppDto> getAppNames() {
        List<String> appNames = camdxLogDao.getAppNames();
        return appNames.stream()
                .map(name -> {
                    LogRegisterAppDto dto = new LogRegisterAppDto();
                    dto.setName(name);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}