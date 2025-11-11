package com.internal.feature.telegram_alerts.service.serviceImpl;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.feature.telegram_alerts.service.AlertsOpenAccOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountTelegramAlertServiceImpl implements AlertsOpenAccOnlineService {

    private final TelegramService telegramService;
    private static final String SEPARATOR = "--------------------";

    @Override
    public void sendTelegramAccountOnlineError(String idNumber, OpenAccStatusEnum status, StringBuilder remarkBuilder) {
        try {
            String body = String.format("NID: %s\nRemark: %s",
                    escapeMarkdown(idNumber), escapeMarkdown(remarkBuilder.toString()));

            String message = buildStandardMessage("Account Online Error", body, status.name(), "-");
            telegramService.sendMarkdownMessage(message);
        } catch (Exception e) {
            log.error("Telegram alert sending failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendTelegramAmlProcess(AmlStatusDto amlDto) {
        if (amlDto == null || amlDto.getCustomerInfo() == null) {
            log.warn("AML Telegram could not send - dto or customer info missing");
            return;
        }

        CustomerAmlDto c = amlDto.getCustomerInfo();
        String header = "*AML Account Online*";
        String customerName = getCustomerDisplayName(c, amlDto.getStatus());

        // Format DOB reliably, support single-digit month/day
        String dobFormatted = formatDob(c.getDateOfBirth());

        String body = String.format(
                "Name: %s\nID: %s\nDOB: %s\nNationality: %s\nAddress: %s",
                escapeMarkdown(getOrNA(customerName)),
                escapeMarkdown(getOrNA(c.getLegalId())),
                escapeMarkdown(getOrNA(dobFormatted)),
                escapeMarkdown(getOrNA(c.getNationality())),
                escapeMarkdown(getOrNA(c.getLegalAddress()))
        );

        String statusText = amlDto.getStatus() != null ? amlDto.getStatus().name() : "N/A";
        String byUser = getProcessedUserName(amlDto);

        // Use AML record creation time if available, else fallback to now
        String timeFormatted;
        if (amlDto.getCreatedAt() != null) {
            timeFormatted = amlDto.getUpdatedAt()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            timeFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        String footer = String.format("Status: %s\nBy: %s\nTime: %s",
                escapeMarkdown(statusText), escapeMarkdown(getOrNA(byUser)), escapeMarkdown(timeFormatted));

        String message = header + "\n" + SEPARATOR + "\n" + body + "\n" + SEPARATOR + "\n" + footer;

        telegramService.sendMarkdownUATMonitorMessage(message);
    }

    private String formatDob(String dob) {
        if (dob == null || dob.isEmpty()) return "N/A";
        try {
            // Accepts both single-digit and double-digit months/days
            DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-M-d")
                    .parseStrict()
                    .toFormatter();
            LocalDate date = LocalDate.parse(dob, inputFormatter);
            return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        } catch (Exception e) {
            log.warn("Failed to parse DOB '{}': {}", dob, e.getMessage());
            return dob; // fallback to original string
        }
    }

    private String getOrNA(String text) {
        return (text == null || text.isEmpty()) ? "N/A" : text;
    }

    private String getCustomerDisplayName(CustomerAmlDto customer, com.internal.enumation.AmlStatusEnum status) {
        String name = joinNonNull(customer.getGivenName(), customer.getFamilyName());
        if (name.isEmpty()) {
            name = customer.getLegalId();
        }

        if (status == com.internal.enumation.AmlStatusEnum.PENDING && name.length() > 0) {
            name = name.substring(0, 1);
        }
        return name;
    }

    private String getProcessedUserName(AmlStatusDto amlDto) {
        if (amlDto == null || amlDto.getStatus() == null) return "N/A";

        switch (amlDto.getStatus()) {
            case PENDING:
                return "N/A";
            case APPROVE:
                if (amlDto.getApprovedBy() != null) {
                    if (amlDto.getApprovedBy().getFullName() != null && !amlDto.getApprovedBy().getFullName().isEmpty()) {
                        return amlDto.getApprovedBy().getFullName();
                    } else if (amlDto.getApprovedBy().getIdCard() != null && !amlDto.getApprovedBy().getIdCard().isEmpty()) {
                        return amlDto.getApprovedBy().getIdCard();
                    }
                }
                return "N/A"; // If no user info, fallback to N/A
            case REJECT:
                if (amlDto.getRejectedBy() != null) {
                    if (amlDto.getRejectedBy().getFullName() != null && !amlDto.getRejectedBy().getFullName().isEmpty()) {
                        return amlDto.getRejectedBy().getFullName();
                    } else if (amlDto.getRejectedBy().getIdCard() != null && !amlDto.getRejectedBy().getIdCard().isEmpty()) {
                        return amlDto.getRejectedBy().getIdCard();
                    }
                }
                return "N/A"; // If no user info, fallback to N/A
            default:
                return "N/A";
        }
    }

    private String buildStandardMessage(String header, String body, String status, String user) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "*" + escapeMarkdown(header) + "*\n" +
                SEPARATOR + "\n" +
                body + "\n" +
                SEPARATOR + "\n" +
                "Status: " + escapeMarkdown(status) + "\n" +
                "By: " + getOrNA(user) + "\n" +
                "Time: " + now;
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("~", "\\~").replace("`", "\\`")
                .replace(">", "\\>").replace("#", "\\#")
                .replace("+", "\\+")
                .replace("=", "\\=").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}").replace(".", "\\.")
                .replace("!", "\\!");
    }

    private String joinNonNull(String a, String b) {
        StringBuilder sb = new StringBuilder();
        if (a != null && !a.isEmpty()) sb.append(a.trim());
        if (b != null && !b.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(b.trim());
        }
        return sb.toString();
    }
}
