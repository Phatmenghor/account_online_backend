package com.internal.feature.attendance.service.impl;

import com.internal.config.TelegramConfig;
import com.internal.feature.attendance.models.AttendanceEntity;
import com.internal.feature.attendance.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationServiceImpl implements TelegramNotificationService {

    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";

    private static final ZoneId PHNOM_PENH = ZoneId.of("Asia/Phnom_Penh");

    // ✅ Use 12-hour time format with AM/PM
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(PHNOM_PENH);

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @Override
    @Async
    public void sendAttendanceRequestNotification(AttendanceEntity attendance) {
        if (!telegramConfig.isEnabled()) {
            log.debug("Telegram notification is disabled");
            return;
        }

        try {
            String message = buildNewRequestMessage(attendance);
            sendTelegramMessage(message);
            log.info("Telegram notification sent for new attendance request ID: {}", attendance.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram notification for attendance request: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendAttendanceApprovalNotification(AttendanceEntity attendance) {
        if (!telegramConfig.isEnabled()) {
            log.debug("Telegram notification is disabled");
            return;
        }

        try {
            String message = buildApprovalMessage(attendance);
            sendTelegramMessage(message);
            log.info("Telegram approval notification sent for attendance ID: {}", attendance.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram approval notification: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendAttendanceRejectionNotification(AttendanceEntity attendance) {
        if (!telegramConfig.isEnabled()) {
            log.debug("Telegram notification is disabled");
            return;
        }

        try {
            String message = buildRejectionMessage(attendance);
            sendTelegramMessage(message);
            log.info("Telegram rejection notification sent for attendance ID: {}", attendance.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram rejection notification: {}", e.getMessage(), e);
        }
    }

    private void sendTelegramMessage(String message) {
        String url = String.format(TELEGRAM_API_URL, telegramConfig.getToken());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chat_id", telegramConfig.getChatId());
        requestBody.put("text", message);
        requestBody.put("parse_mode", "HTML");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Telegram message sent successfully");
            } else {
                log.warn("Telegram API returned status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling Telegram API: {}", e.getMessage());
            throw e;
        }
    }

    // ✅ Build message for new request
    private String buildNewRequestMessage(AttendanceEntity attendance) {
        String typeFormatted = formatAttendanceType(attendance.getType().name());
        String leaveTypeFormatted = formatLeaveRequest(attendance.getLeaveRequest().name());

        return String.format(
                "📢 <b>New Attendance Request</b>\n\n" +
                        "📋 <b>Request ID:</b> #%d\n" +
                        "👤 <b>Employee:</b> %s\n" +
                        "🆔 <b>ID Card:</b> %s\n" +
                        "💼 <b>Position:</b> %s\n" +
                        "📝 <b>Type:</b> %s\n" +
                        "⏰ <b>Leave Type:</b> %s\n" +
                        "📅 <b>Period:</b> %s to %s\n" +
                        "⏱ <b>Duration:</b> %s day(s)\n" +
                        "📄 <b>Reason:</b> %s\n" +
                        "🕐 <b>Submitted:</b> %s\n\n" +
                        "⚠️ <b>Status:</b> PENDING APPROVAL",
                attendance.getId(),
                nullToNA(attendance.getUser().getFullName()),
                nullToNA(attendance.getUser().getUsername()),
                nullToNA(attendance.getUser().getPosition()),
                nullToNA(typeFormatted),
                nullToNA(leaveTypeFormatted),
                formatDate(attendance.getStartDate()),
                formatDate(attendance.getEndDate()),
                formatDays(attendance.getTotalDays()),
                nullToNA(attendance.getReason()),
                formatDateTime(attendance.getCreatedAt())
        );
    }

    // ✅ Approval message
    private String buildApprovalMessage(AttendanceEntity attendance) {
        String typeFormatted = formatAttendanceType(attendance.getType().name());
        String leaveTypeFormatted = formatLeaveRequest(attendance.getLeaveRequest().name());

        StringBuilder message = new StringBuilder();
        message.append("✅ <b>Attendance Request APPROVED</b>\n\n")
                .append(String.format("📋 <b>Request ID:</b> #%d\n", attendance.getId()))
                .append(String.format("👤 <b>Employee:</b> %s\n", nullToNA(attendance.getUser().getFullName())))
                .append(String.format("🆔 <b>ID Card:</b> %s\n", nullToNA(attendance.getUser().getUsername())))
                .append(String.format("📝 <b>Type:</b> %s\n", nullToNA(typeFormatted)))
                .append(String.format("⏰ <b>Leave Type:</b> %s\n", nullToNA(leaveTypeFormatted)))
                .append(String.format("📅 <b>Period:</b> %s to %s\n",
                        formatDate(attendance.getStartDate()), formatDate(attendance.getEndDate())))
                .append(String.format("⏱ <b>Duration:</b> %s day(s)\n", formatDays(attendance.getTotalDays())))
                .append(String.format("👨‍💼 <b>Approved By:</b> %s\n",
                        attendance.getApprovedBy() != null
                                ? nullToNA(attendance.getApprovedBy().getFullName())
                                : "N/A"))
                .append(String.format("🕐 <b>Approved At:</b> %s\n", formatDateTime(attendance.getApprovedAt())))
                .append(String.format("💬 <b>Notes:</b> %s\n", nullToNA(attendance.getApprovalNotes())));

        return message.toString();
    }

    // ✅ Rejection message
    private String buildRejectionMessage(AttendanceEntity attendance) {
        String typeFormatted = formatAttendanceType(attendance.getType().name());
        String leaveTypeFormatted = formatLeaveRequest(attendance.getLeaveRequest().name());

        StringBuilder message = new StringBuilder();
        message.append("❌ <b>Attendance Request REJECTED</b>\n\n")
                .append(String.format("📋 <b>Request ID:</b> #%d\n", attendance.getId()))
                .append(String.format("👤 <b>Employee:</b> %s\n", nullToNA(attendance.getUser().getFullName())))
                .append(String.format("🆔 <b>ID Card:</b> %s\n", nullToNA(attendance.getUser().getUsername())))
                .append(String.format("📝 <b>Type:</b> %s\n", nullToNA(typeFormatted)))
                .append(String.format("⏰ <b>Leave Type:</b> %s\n", nullToNA(leaveTypeFormatted)))
                .append(String.format("📅 <b>Period:</b> %s to %s\n",
                        formatDate(attendance.getStartDate()), formatDate(attendance.getEndDate())))
                .append(String.format("⏱ <b>Duration:</b> %s day(s)\n", formatDays(attendance.getTotalDays())))
                .append(String.format("👨‍💼 <b>Rejected By:</b> %s\n",
                        attendance.getApprovedBy() != null
                                ? nullToNA(attendance.getApprovedBy().getFullName())
                                : "N/A"))
                .append(String.format("🕐 <b>Rejected At:</b> %s\n", formatDateTime(attendance.getApprovedAt())))
                .append(String.format("💬 <b>Reason:</b> %s\n", nullToNA(attendance.getApprovalNotes())));

        return message.toString();
    }

    // ✅ Helpers
    private String nullToNA(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value.trim();
    }

    private String formatDateTime(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return "N/A";
        return utcDateTime.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(PHNOM_PENH)
                .format(DATETIME_FORMATTER);
    }

    private String formatDate(LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : "N/A";
    }

    private String formatAttendanceType(String type) {
        if (type == null) return "N/A";
        String[] words = type.replace("_", " ").toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0)
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return result.toString().trim();
    }

    private String formatLeaveRequest(String leaveRequest) {
        if (leaveRequest == null) return "N/A";
        switch (leaveRequest) {
            case "MORNING": return "Morning (Half Day)";
            case "AFTERNOON": return "Afternoon (Half Day)";
            case "FULL_DAY": return "Full Day";
            default: return formatAttendanceType(leaveRequest);
        }
    }

    private String formatDays(Double days) {
        if (days == null) return "N/A";
        return (days % 1 == 0) ? String.valueOf(days.intValue()) : String.format("%.1f", days);
    }
}
