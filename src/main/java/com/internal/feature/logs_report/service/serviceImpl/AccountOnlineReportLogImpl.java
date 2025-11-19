package com.internal.feature.logs_report.service.serviceImpl;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.logs_report.dto.request.AccountOnlineReportLogDto;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportProjection;
import com.internal.feature.logs_report.dto.response.AccountOnlineReportResponse;
import com.internal.feature.logs_report.mapper.AccountOnlineReportMapper;
import com.internal.feature.logs_report.model.AccountOnlineReportLog;
import com.internal.feature.logs_report.repository.AccountOnlineReportLogRepository;
import com.internal.feature.logs_report.service.AccountOnlineReportLogService;
import com.internal.feature.logs_report.specification.AccountOnlineReportLogSpecification;
import com.internal.feature.telegram_alerts.service.AlertsOpenAccOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountOnlineReportLogImpl implements AccountOnlineReportLogService {

    private final AccountOnlineReportLogRepository repository;
    private final boolean passwordProtectionEnabled = false; // set true in future
    private final AccountOnlineReportMapper accountOnlineReportMapper;

    @Override
    public void saveLogReport(String idNumber, OpenAccStatusEnum status, String remark) {
        log.info("Saving account online report log - ID number: {}, Status: {}", idNumber, status);

        AccountOnlineReportLog logEntry = AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remark)
                .build();

        repository.save(logEntry);
    }

    @Override
    public void createAccountOpeningLog(String idNumber, OpenAccStatusEnum status, String stepInfo, Exception exception) {
        log.info("Creating account opening log - ID: {}, Status: {}, Step: {}", idNumber, status, stepInfo);

        StringBuilder remarkBuilder = new StringBuilder();
        remarkBuilder.append("Step: ").append(stepInfo);

        if (exception != null) {
            remarkBuilder.append(" | Error: ").append(exception.getClass().getSimpleName());
            remarkBuilder.append(" | Message: ").append(exception.getMessage());
        }

        AccountOnlineReportLog onlineReportLog = AccountOnlineReportLog.builder()
                .idNumber(idNumber)
                .status(status)
                .remark(remarkBuilder.toString())
                .build();

        //push tele ACL internal
//        try {
//            accountOnlineErrorService.sendTelegramAccountOnlineError(idNumber,status,remarkBuilder);
//        } catch (Exception e) {
//            log.error("Failed to send Telegram notification, but logs was created: {}",
//                    e.getMessage());
//        }

        repository.save(onlineReportLog);
    }

    @Override
    public byte[] generateExcel(AccountOnlineReportLogDto filterDto) throws Exception {

        List<AccountOnlineReportLog> logs = repository.findAll(
                AccountOnlineReportLogSpecification.filter(filterDto),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream tempOut = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("NID Validation Logs");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle statusSuccessStyle = createStatusStyle(workbook, IndexedColors.GREEN);
            CellStyle statusFailureStyle = createStatusStyle(workbook, IndexedColors.RED);
            CellStyle titleStyle = createTitleStyle(workbook);

            int currentRow = 0;

            // Title row
            currentRow = generateTitleRow(sheet, currentRow, titleStyle);

            // Metadata row
            currentRow = generateMetadataRow(sheet, currentRow, logs, dataStyle);

            // Empty row
            currentRow++;

            // Header row
            Row headerRow = sheet.createRow(currentRow++);
            headerRow.setHeightInPoints(25);
            String[] headers = {"ID", "NID", "Request", "Response", "Status", "Created At", "Updated At", "Created By", "Updated By"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Data rows
            generateDateRow(logs, sheet, currentRow, dataStyle, workbook, statusSuccessStyle, statusFailureStyle, dtf, dateStyle);

            // Auto-size and adjust column width
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            // Merge title and metadata
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, headers.length - 1));

            // Freeze header rows
            sheet.createFreezePane(0, 4);

            // Write workbook to temp output stream first
            workbook.write(tempOut);

            // Apply password protection
            return returnExcelResult(filterDto, tempOut);

        }
    }

    @Override
    public List<AccountOnlineReportResponse> getReportByDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.plusDays(1).atStartOfDay();

        List<AccountOnlineReportProjection> projections = repository.getReportByDateRange(fromDateTime, toDateTime);

        return accountOnlineReportMapper.projectionsToResponses(projections);
    }

    private static int generateTitleRow(Sheet sheet, int currentRow, CellStyle titleStyle) {
        Row titleRow = sheet.createRow(currentRow++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("NID Validation Failure Logs Report");
        titleCell.setCellStyle(titleStyle);
        titleRow.setHeightInPoints(30);
        return currentRow;
    }

    private static int generateMetadataRow(Sheet sheet, int currentRow, List<AccountOnlineReportLog> logs, CellStyle dataStyle) {
        Row metadataRow = sheet.createRow(currentRow++);
        Cell metadataCell = metadataRow.createCell(0);
        metadataCell.setCellValue("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " | Total Records: " + logs.size());
        metadataCell.setCellStyle(dataStyle);
        return currentRow;
    }

    private void generateDateRow(List<AccountOnlineReportLog> logs, Sheet sheet, int currentRow, CellStyle dataStyle, Workbook workbook, CellStyle statusSuccessStyle, CellStyle statusFailureStyle, DateTimeFormatter dtf, CellStyle dateStyle) {
        for (int i = 0; i < logs.size(); i++) {
            AccountOnlineReportLog log = logs.get(i);
            Row row = sheet.createRow(currentRow++);
            row.setHeightInPoints(20);

            CellStyle rowStyle = i % 2 == 0 ? dataStyle : createAlternateRowStyle(workbook);

            createStyledCell(row, 0, log.getId().toString(), rowStyle);
            createStyledCell(row, 1, log.getIdNumber(), rowStyle);

            Cell statusCell = row.createCell(4);
            String statusValue = log.getStatus() != null ? log.getStatus().name() : "";
            statusCell.setCellValue(statusValue);
            if (statusValue.equalsIgnoreCase("SUCCESS") || statusValue.equalsIgnoreCase("VALID")) {
                statusCell.setCellStyle(statusSuccessStyle);
            } else if (statusValue.equalsIgnoreCase("FAILURE") || statusValue.equalsIgnoreCase("INVALID")) {
                statusCell.setCellStyle(statusFailureStyle);
            } else {
                statusCell.setCellStyle(rowStyle);
            }

            createStyledCell(row, 5, log.getCreatedAt() != null ? log.getCreatedAt().format(dtf) : "", dateStyle);
            createStyledCell(row, 6, log.getUpdatedAt() != null ? log.getUpdatedAt().format(dtf) : "", dateStyle);
            createStyledCell(row, 7, log.getCreatedBy() != null ? log.getCreatedBy() : "", rowStyle);
            createStyledCell(row, 8, log.getUpdatedBy() != null ? log.getUpdatedBy() : "", rowStyle);
        }
    }

    private byte[] returnExcelResult(AccountOnlineReportLogDto filterDto, ByteArrayOutputStream tempOut) throws IOException {
        if (passwordProtectionEnabled && filterDto.getPassword() != null && !filterDto.getPassword().isEmpty()) {
            POIFSFileSystem fs = new POIFSFileSystem();
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.standard);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(filterDto.getPassword());

            try (ByteArrayOutputStream finalOut = new ByteArrayOutputStream()) {
                try (java.io.OutputStream encOut = encryptor.getDataStream(fs)) {
                    encOut.write(tempOut.toByteArray());
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                fs.writeFilesystem(finalOut);
                return finalOut.toByteArray();
            }
        } else {
            // No password protection, just return the normal bytes
            return tempOut.toByteArray();
        }
    }

    private void createStyledCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        return style;
    }

    private CellStyle createAlternateRowStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createStatusStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
