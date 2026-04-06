package com.internal.feature.telegram_alerts.service.serviceImpl;

import com.internal.enumation.AmlStatusEnum;
import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.feature.telegram_alerts.service.AlertsOpenAccOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.internal.feature.logs_report.service.CustomerImageService;
import org.springframework.core.io.Resource;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountTelegramAlertServiceImpl implements AlertsOpenAccOnlineService {

    private final TelegramService telegramService;
    private final CustomerImageService customerImageService;
    @Value("${telegram.bot.uat-acl-chat-id}")
    private String chatId_acl_internal;

    @Value("${telegram.bot.uat-monitor-chat-id}")
    private String chatId_uat_monitor;

    @Value("${telegram.bot.uat-dev-team-chat-id:}")
    private String chatId_dev_team;

    @Value("${telegram.bot.compliance-mention}")
    private String complianceMention;

    private static final String SEPARATOR = "--------------------";

    @Override
    public void sendTelegramAccountOnlineError(String idNumber, OpenAccStatusEnum status, StringBuilder remarkBuilder) {
        try {
            StringBuilder bodyBuilder = new StringBuilder();
            appendIfNotEmpty(bodyBuilder, "NID", idNumber);
            appendIfNotEmpty(bodyBuilder, "Remark", remarkBuilder != null ? remarkBuilder.toString() : null);

            String message = buildStandardMessage("Account Online Error", bodyBuilder.toString(), status.name(), "-");
            // Changed to send to ACL (Error) channel as requested
            telegramService.sendMarkdownAclInternalMessage(message);
        } catch (Exception e) {
            log.error("Telegram alert sending failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendTelegramInternalError(String idNumber, OpenAccStatusEnum status, StringBuilder remarkBuilder) {
        // Disabled - errors are sent via MonitoringService with detailed context
    }

    @Override
    public void sendTelegramAmlProcess(AmlStatusDto amlDto) {
        if (amlDto == null) {
            log.warn("AML Telegram could not send - dto missing");
            return;
        }

        String header = "*AML Account Online*";
        StringBuilder bodyBuilder = new StringBuilder();

        // Customer display name
        String customerName = getCustomerDisplayName(
                amlDto.getCustomerInfo().getGivenName(),
                amlDto.getCustomerInfo().getFamilyName(),
                amlDto.getCustomerInfo().getFirstNameKh(),
                amlDto.getCustomerInfo().getLastNameKh(),
                amlDto.getStatus(),
                amlDto.getCustomerInfo().getLegalId());

        // Append all fields for all statuses
        appendIfNotEmpty(bodyBuilder, "Name", customerName);
        appendIfNotEmpty(bodyBuilder, "Legal ID", amlDto.getCustomerInfo().getLegalId());
        appendIfNotEmpty(bodyBuilder, "Gender", amlDto.getCustomerInfo().getGender());
        appendIfNotEmpty(bodyBuilder, "DOB", formatDob(amlDto.getCustomerInfo().getDateOfBirth()));
        appendIfNotEmpty(bodyBuilder, "Nationality", amlDto.getCustomerInfo().getNationality());
        appendIfNotEmpty(bodyBuilder, "Current Address", amlDto.getCurrentAddressName());
        appendIfNotEmpty(bodyBuilder, "Phone Number", amlDto.getCustomerInfo().getPhoneNumber());
        appendIfNotEmpty(bodyBuilder, "Place of Birth", amlDto.getPlaceOfBirthName());
        appendIfNotEmpty(bodyBuilder, "Marital Status", amlDto.getMaritalStatus());
        appendIfNotEmpty(bodyBuilder, "ID Issued", amlDto.getCustomerInfo().getIssuedDate());
        appendIfNotEmpty(bodyBuilder, "ID Expired", amlDto.getCustomerInfo().getExpiredDate());
        appendIfNotEmpty(bodyBuilder, "Occupation", amlDto.getOccupationStatus());
        appendIfNotEmpty(bodyBuilder, "Risk Level", amlDto.getRiskLevel());
        appendIfNotEmpty(bodyBuilder, "Action Taken", amlDto.getActionTaken());
        appendIfNotEmpty(bodyBuilder, "Service Name", amlDto.getServiceName());
        if (amlDto.getTotalRulesScore() > 0) {
            bodyBuilder.append("- Total Rules Score: ").append(amlDto.getTotalRulesScore()).append("\n");
        }
        appendIfNotEmpty(bodyBuilder, "Rules Triggered", String.join(", ", amlDto.getRulesTriggered()));
        appendIfNotEmpty(bodyBuilder, "Transaction ID", amlDto.getTrxnID());
        appendIfNotEmpty(bodyBuilder, "Remarks", amlDto.getRemarks());

        // Footer: "By" is empty for PENDING
        String statusText = amlDto.getStatus() != null ? amlDto.getStatus().name() : "N/A";
        String byUser = amlDto.getStatus() == AmlStatusEnum.PENDING ? "" : getProcessedUserName(amlDto);
        String timeFormatted = amlDto.getCreatedAt() != null
                ? amlDto.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String footer = "Status: " + escapeMarkdown(statusText) + "\n" +
                (byUser.isEmpty() ? "" : "By: " + escapeMarkdown(byUser) + "\n") +
                "Time: " + escapeMarkdown(timeFormatted);

        String message = header + "\n" + SEPARATOR + "\n" + bodyBuilder + SEPARATOR + "\n" + footer;

        // Logic for High Risk to attach images
        if ("HIGH".equalsIgnoreCase(amlDto.getRiskLevel())) {

            // Send main message to Monitor Channel as requested
            telegramService.sendMarkdownAccountOnlineMonitorMessage(message);

            // Send images to Monitor Channel as requested
            try {
                String customerId = amlDto.getCustomerInfo().getLegalId();
                if (customerId != null) {
                    Resource nidResource = customerImageService.getNidImageResourceForEmail(customerId);
                    if (nidResource != null && nidResource.exists()) {
                        telegramService.sendPhoto(chatId_uat_monitor, "NID - " + customerId, nidResource);
                    }

                    Resource selfieResource = customerImageService.getSelfieImageResourceForEmail(customerId);
                    if (selfieResource != null && selfieResource.exists()) {
                        telegramService.sendPhoto(chatId_uat_monitor, "Selfie - " + customerId, selfieResource);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to attach images for High Risk AML alert: {}", e.getMessage());
            }

        } else {
            telegramService.sendMarkdownAccountOnlineMonitorMessage(message);
        }

        // Send detailed alert to Dev Team for HIGH RISK cases
        if ("HIGH".equalsIgnoreCase(amlDto.getRiskLevel())) {
            sendDetailedAmlAlertToDevTeam(amlDto);
        }
    }

    /**
     * Send detailed AML alert to Dev Team for debugging and monitoring
     */
    private void sendDetailedAmlAlertToDevTeam(AmlStatusDto amlDto) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timeFormatted = amlDto.getCreatedAt() != null
                    ? amlDto.getCreatedAt().format(formatter)
                    : LocalDateTime.now().format(formatter);

            StringBuilder detailedMsg = new StringBuilder();
            detailedMsg.append("*AML Account Online - HIGH RISK*\n")
                    .append("--------------------\n")
                    .append("- Name: ").append(getCustomerDisplayName(
                            amlDto.getCustomerInfo().getGivenName(),
                            amlDto.getCustomerInfo().getFamilyName(),
                            amlDto.getCustomerInfo().getFirstNameKh(),
                            amlDto.getCustomerInfo().getLastNameKh(),
                            amlDto.getStatus(),
                            amlDto.getCustomerInfo().getLegalId())).append("\n")
                    .append("- Legal ID: `").append(amlDto.getCustomerInfo().getLegalId()).append("`\n")
                    .append("- Gender: `").append(amlDto.getCustomerInfo().getGender()).append("`\n")
                    .append("- DOB: `").append(formatDob(amlDto.getCustomerInfo().getDateOfBirth())).append("`\n")
                    .append("- Nationality: `").append(amlDto.getCustomerInfo().getNationality()).append("`\n")
                    .append("- Current Address: `").append(amlDto.getCurrentAddressName()).append("`\n")
                    .append("- Phone Number: `").append(amlDto.getCustomerInfo().getPhoneNumber()).append("`\n")
                    .append("- Place of Birth: `").append(amlDto.getPlaceOfBirthName()).append("`\n")
                    .append("- Marital Status: `").append(amlDto.getMaritalStatus()).append("`\n")
                    .append("- ID Issued: `").append(amlDto.getCustomerInfo().getIssuedDate()).append("`\n")
                    .append("- ID Expired: `").append(amlDto.getCustomerInfo().getExpiredDate()).append("`\n")
                    .append("- Occupation: `").append(amlDto.getOccupationStatus()).append("`\n")
                    .append("- Risk Level: `").append(amlDto.getRiskLevel()).append("`\n")
                    .append("- Service Name: `").append(amlDto.getServiceName()).append("`\n");

            if (amlDto.getTotalRulesScore() > 0) {
                detailedMsg.append("- Total Rules Score: `").append(amlDto.getTotalRulesScore()).append("`\n");
            }

            // Format rules triggered (stored as JSON string)
            String rulesTriggered = amlDto.getRulesTriggered();
            if (rulesTriggered != null && !rulesTriggered.isEmpty()) {
                detailedMsg.append("- Rules Triggered: `").append(escapeMarkdown(rulesTriggered)).append("`\n");
            }

            detailedMsg.append("- Transaction ID: `").append(amlDto.getTrxnID()).append("`\n")
                    .append("- Remarks: `").append(amlDto.getRemarks()).append("`\n")
                    .append("--------------------\n")
                    .append("Status: `").append(amlDto.getStatus().name()).append("`\n");

            if (amlDto.getApprovedBy() != null && amlDto.getApprovedBy().getFullName() != null) {
                detailedMsg.append("By: ").append(amlDto.getApprovedBy().getFullName()).append("\n");
            }

            detailedMsg.append("Time: `").append(timeFormatted).append("`");

            telegramService.sendDetailedErrorToDevTeam(detailedMsg.toString());
        } catch (Exception e) {
            log.debug("Detailed AML alert not sent: {}", e.getMessage());
        }
    }

    private String formatDob(String dob) {
        if (dob == null || dob.isEmpty())
            return null;
        try {
            DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-M-d")
                    .parseStrict()
                    .toFormatter();
            LocalDate date = LocalDate.parse(dob, inputFormatter);
            return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        } catch (Exception e) {
            log.warn("Failed to parse DOB '{}': {}", dob, e.getMessage());
            return dob;
        }
    }

    private String getCustomerDisplayName(
            String givenName,
            String familyName,
            String firstNameKh,
            String lastNameKh,
            AmlStatusEnum status,
            String legalId) {
        String name = joinNonNull(givenName, familyName);
        if (name.isEmpty())
            name = joinNonNull(firstNameKh, lastNameKh);
        if (name.isEmpty())
            name = legalId != null ? legalId : "";
        return name;
    }

    private String getProcessedUserName(AmlStatusDto amlDto) {
        if (amlDto == null || amlDto.getStatus() == null)
            return "N/A";

        return switch (amlDto.getStatus()) {
            case PENDING -> ""; // Hide "By" for pending
            case APPROVE -> {
                if (amlDto.getApprovedBy() != null) {
                    if (amlDto.getApprovedBy().getFullName() != null && !amlDto.getApprovedBy().getFullName().isEmpty())
                        yield amlDto.getApprovedBy().getFullName();
                    if (amlDto.getApprovedBy().getIdCard() != null && !amlDto.getApprovedBy().getIdCard().isEmpty())
                        yield amlDto.getApprovedBy().getIdCard();
                }
                yield "";
            }
            case REJECT -> {
                if (amlDto.getRejectedBy() != null) {
                    if (amlDto.getRejectedBy().getFullName() != null && !amlDto.getRejectedBy().getFullName().isEmpty())
                        yield amlDto.getRejectedBy().getFullName();
                    if (amlDto.getRejectedBy().getIdCard() != null && !amlDto.getRejectedBy().getIdCard().isEmpty())
                        yield amlDto.getRejectedBy().getIdCard();
                }
                yield "";
            }
            default -> "";
        };
    }

    private void appendIfNotEmpty(StringBuilder sb, String fieldName, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append("- ").append(fieldName).append(": ").append(escapeMarkdown(value)).append("\n");
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null)
            return "";
        // Escape only characters that actually break Markdown
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("~", "\\~")
                .replace("`", "\\`");
    }

    private String joinNonNull(String a, String b) {
        StringBuilder sb = new StringBuilder();
        if (a != null && !a.isEmpty())
            sb.append(a.trim());
        if (b != null && !b.isEmpty()) {
            if (!sb.isEmpty())
                sb.append(" ");
            sb.append(b.trim());
        }
        return sb.toString();
    }

    private String buildStandardMessage(String header, String body, String status, String user) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "*" + escapeMarkdown(header) + "*\n" +
                SEPARATOR + "\n" +
                body + "\n" +
                SEPARATOR + "\n" +
                "Status: " + escapeMarkdown(status) + "\n" +
                (user != null && !user.isEmpty() ? "By: " + escapeMarkdown(user) + "\n" : "") +
                "Time: " + now;
    }
}
