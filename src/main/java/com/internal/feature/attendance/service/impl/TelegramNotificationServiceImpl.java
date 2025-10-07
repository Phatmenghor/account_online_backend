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

import java.time.ZoneId;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.of("Asia/Phnom_Penh"));;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.of("Asia/Phnom_Penh"));;

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
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

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
                attendance.getUser().getFullName() != null ? attendance.getUser().getFullName() : "N/A",
                attendance.getUser().getUsername(),
                attendance.getUser().getPosition() != null ? attendance.getUser().getPosition() : "N/A",
                typeFormatted,
                leaveTypeFormatted,
                attendance.getStartDate().format(DATE_FORMATTER),
                attendance.getEndDate().format(DATE_FORMATTER),
                formatDays(attendance.getTotalDays()),
                attendance.getReason(),
                attendance.getCreatedAt().format(DATETIME_FORMATTER)
        );
    }

    private String buildApprovalMessage(AttendanceEntity attendance) {
        String typeFormatted = formatAttendanceType(attendance.getType().name());
        String leaveTypeFormatted = formatLeaveRequest(attendance.getLeaveRequest().name());

        StringBuilder message = new StringBuilder();
        message.append("✅ <b>Attendance Request APPROVED</b>\n\n");
        message.append(String.format("📋 <b>Request ID:</b> #%d\n", attendance.getId()));
        message.append(String.format("👤 <b>Employee:</b> %s\n",
                attendance.getUser().getFullName() != null ? attendance.getUser().getFullName() : "N/A"));
        message.append(String.format("🆔 <b>ID Card:</b> %s\n", attendance.getUser().getUsername()));
        message.append(String.format("📝 <b>Type:</b> %s\n", typeFormatted));
        message.append(String.format("⏰ <b>Leave Type:</b> %s\n", leaveTypeFormatted));
        message.append(String.format("📅 <b>Period:</b> %s to %s\n",
                attendance.getStartDate().format(DATE_FORMATTER),
                attendance.getEndDate().format(DATE_FORMATTER)));
        message.append(String.format("⏱ <b>Duration:</b> %s day(s)\n", formatDays(attendance.getTotalDays())));

        if (attendance.getApprovedBy() != null) {
            message.append(String.format("👨‍💼 <b>Approved By:</b> %s\n",
                    attendance.getApprovedBy().getFullName() != null ?
                            attendance.getApprovedBy().getFullName() : attendance.getApprovedBy().getUsername()));
        }

        if (attendance.getApprovedAt() != null) {
            message.append(String.format("🕐 <b>Approved At:</b> %s\n",
                    attendance.getApprovedAt().format(DATETIME_FORMATTER)));
        }

        if (attendance.getApprovalNotes() != null && !attendance.getApprovalNotes().trim().isEmpty()) {
            message.append(String.format("💬 <b>Notes:</b> %s\n", attendance.getApprovalNotes()));
        }

        return message.toString();
    }

    private String buildRejectionMessage(AttendanceEntity attendance) {
        String typeFormatted = formatAttendanceType(attendance.getType().name());
        String leaveTypeFormatted = formatLeaveRequest(attendance.getLeaveRequest().name());

        StringBuilder message = new StringBuilder();
        message.append("❌ <b>Attendance Request REJECTED</b>\n\n");
        message.append(String.format("📋 <b>Request ID:</b> #%d\n", attendance.getId()));
        message.append(String.format("👤 <b>Employee:</b> %s\n",
                attendance.getUser().getFullName() != null ? attendance.getUser().getFullName() : "N/A"));
        message.append(String.format("🆔 <b>ID Card:</b> %s\n", attendance.getUser().getUsername()));
        message.append(String.format("📝 <b>Type:</b> %s\n", typeFormatted));
        message.append(String.format("⏰ <b>Leave Type:</b> %s\n", leaveTypeFormatted));
        message.append(String.format("📅 <b>Period:</b> %s to %s\n",
                attendance.getStartDate().format(DATE_FORMATTER),
                attendance.getEndDate().format(DATE_FORMATTER)));
        message.append(String.format("⏱ <b>Duration:</b> %s day(s)\n", formatDays(attendance.getTotalDays())));

        if (attendance.getApprovedBy() != null) {
            message.append(String.format("👨‍💼 <b>Rejected By:</b> %s\n",
                    attendance.getApprovedBy().getFullName() != null ?
                            attendance.getApprovedBy().getFullName() : attendance.getApprovedBy().getUsername()));
        }

        if (attendance.getApprovedAt() != null) {
            message.append(String.format("🕐 <b>Rejected At:</b> %s\n",
                    attendance.getApprovedAt().format(DATETIME_FORMATTER)));
        }

        if (attendance.getApprovalNotes() != null && !attendance.getApprovalNotes().trim().isEmpty()) {
            message.append(String.format("💬 <b>Reason:</b> %s\n", attendance.getApprovalNotes()));
        }

        return message.toString();
    }

    private String formatAttendanceType(String type) {
        String[] words = type.replace("_", " ").toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    private String formatLeaveRequest(String leaveRequest) {
        switch (leaveRequest) {
            case "MORNING":
                return "Morning (Half Day)";
            case "AFTERNOON":
                return "Afternoon (Half Day)";
            case "FULL_DAY":
                return "Full Day";
            default:
                return formatAttendanceType(leaveRequest);
        }
    }

    /**
     * Format days to display properly (e.g., 0.5, 1.0, 2.5)
     */
    private String formatDays(Double days) {
        if (days == null) {
            return "0";
        }
        // If it's a whole number, show without decimal
        if (days % 1 == 0) {
            return String.valueOf(days.intValue());
        }
        // Otherwise show with decimal
        return String.format("%.1f", days);
    }
}