package com.internal.feature.telegram_alerts.service.serviceImpl;

import com.internal.enumation.OpenAccStatusEnum;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.telegram_alerts.config.TelegramService;
import com.internal.feature.telegram_alerts.service.AlertsOpenAccOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAccountTelegramAlertServiceImpl implements AlertsOpenAccOnlineService {

    private final TelegramService telegramService;
    private static final String SEPARATOR = "━━━━━━━━━━━━━━━━━━";

    @Override
    public void sendTelegramAccountOnlineError(String idNumber, OpenAccStatusEnum status, StringBuilder remarkBuilder) {
        try {
            String body = String.format("🪪 NID: %s\n⚙️ Remark: %s",
                    escapeMarkdown(idNumber), escapeMarkdown(remarkBuilder.toString()));

            String message = buildStandardMessage("Account Online Error", body, status.name(), "-");
            telegramService.sendMarkdownUATMonitorMessage(message);
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

        // Header
        String header = "📢 *AML Account Online*";

        // Determine customer display name
        String customerName = getCustomerDisplayName(c, amlDto.getStatus());

        // Body
        String body = String.format(
                "👤 Customer: %s\n🪪 ID: %s\n📅 DOB: %s\n🌍 Nationality: %s\n🏠 Address: %s",
                escapeMarkdown(customerName),
                escapeMarkdown(c.getIdDisplay()),
                escapeMarkdown(c.getDateOfBirth()),
                escapeMarkdown(c.getNationality()),
                escapeMarkdown(c.getLegalAddress())
        );

        // Footer
        String statusText = amlDto.getStatus() != null ? amlDto.getStatus().name() : "-";
        String byUser = getProcessedUserName(amlDto);

        String footer = String.format("📎 Status: %s\n🧑‍⚖️ By: %s\n⏰ Time: %s",
                statusText, byUser, LocalDateTime.now().toString());

        // Assemble message with separator
        String message = header + "\n" + SEPARATOR + "\n" + body + "\n" + SEPARATOR + "\n" + footer;

        telegramService.sendMarkdownUATMonitorMessage(message);
    }

    /**
     * Returns the customer display name:
     * - Full name if exists
     * - Fallback to ID if no full name
     * - If PENDING, return first initial only
     */
    private String getCustomerDisplayName(CustomerAmlDto customer, com.internal.enumation.AmlStatusEnum status) {
        String name = joinNonNull(customer.getGivenName(), customer.getFamilyName());
        if (name.isEmpty()) {
            name = customer.getIdDisplay();
        }

        if (status == com.internal.enumation.AmlStatusEnum.PENDING && name.length() > 0) {
            name = name.substring(0, 1); // first initial only
        }
        return name;
    }

    /**
     * Returns the "By" user for the footer:
     * - PENDING: "-"
     * - APPROVE: approvedBy full name
     * - REJECT: rejectedBy full name
     */
    private String getProcessedUserName(AmlStatusDto amlDto) {
        if (amlDto == null || amlDto.getStatus() == null) return "-";

        switch (amlDto.getStatus()) {
            case PENDING:
                return "-";
            case APPROVE:
                if (amlDto.getApprovedBy() != null && amlDto.getApprovedBy().getFullName() != null) {
                    return escapeMarkdown(amlDto.getApprovedBy().getFullName());
                } else {
                    assert amlDto.getApprovedBy() != null;
                    return escapeMarkdown(amlDto.getApprovedBy().getIdCard());
                }
            case REJECT:
                return amlDto.getRejectedBy() != null && amlDto.getRejectedBy().getFullName() != null
                        ? escapeMarkdown(amlDto.getRejectedBy().getFullName()) : escapeMarkdown(amlDto.getApprovedBy().getIdCard());
            default:
                log.warn("Unsupported AML status: {}", amlDto.getStatus());
                return "-";
        }
    }

    private String buildStandardMessage(String header, String body, String status, String user) {
        String now = LocalDateTime.now().toString();
        return new StringBuilder()
                .append("📢 *").append(escapeMarkdown(header)).append("*\n")
                .append(SEPARATOR).append("\n")
                .append(body).append("\n")
                .append(SEPARATOR).append("\n")
                .append("📎 Status: ").append(escapeMarkdown(status)).append("\n")
                .append("🧑‍⚖️ By: ").append(escapeMarkdown(user)).append("\n")
                .append("⏰ Time: ").append(now)
                .toString();
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("_", "\\_").replace("*", "\\*")
                .replace("[", "\\[").replace("]", "\\]").replace("(", "\\(")
                .replace(")", "\\)").replace("~", "\\~").replace("`", "\\`")
                .replace(">", "\\>").replace("#", "\\#").replace("+", "\\+")
                .replace("-", "\\-").replace("=", "\\=").replace("|", "\\|")
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
