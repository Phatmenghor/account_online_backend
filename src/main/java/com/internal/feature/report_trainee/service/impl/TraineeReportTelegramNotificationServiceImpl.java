package com.internal.feature.report_trainee.service.impl;

import com.internal.config.TelegramConfig;
import com.internal.feature.auth.models.UserEntity;
import com.internal.feature.auth.repository.UserRepository;
import com.internal.feature.report_trainee.models.TraineeReport;
import com.internal.feature.report_trainee.service.TraineeReportTelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraineeReportTelegramNotificationServiceImpl implements TraineeReportTelegramNotificationService {

    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";
    private static final ZoneId PHNOM_PENH = ZoneId.of("Asia/Phnom_Penh");

    // 👉 12-hour format with AM/PM indicator
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @Override
    @Async
    public void sendReportCreatedNotification(TraineeReport report) {
        if (!telegramConfig.isEnabled()) return;
        try {
            sendTelegramMessage(buildReportMessage(report, "Created"));
            log.info("Telegram notification sent for new trainee report ID: {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram notification for trainee report creation: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendReportUpdatedNotification(TraineeReport report) {
        if (!telegramConfig.isEnabled()) return;
        try {
            sendTelegramMessage(buildReportMessage(report, "Updated"));
            log.info("Telegram notification sent for updated trainee report ID: {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram notification for trainee report update: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendReportDeletedNotification(Long reportId, String deletedBy) {
        if (!telegramConfig.isEnabled()) return;
        try {
            UserEntity userEntity = userRepository.findByUsername(deletedBy)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            StringBuilder message = new StringBuilder();
            message.append("🗑️ <b>Trainee Report Deleted</b>\n")
                    .append("─────────────────────────\n")
                    .append(String.format("🆔 <b>Report ID:</b> <code>INTERNAL#%d</code>\n", reportId))
                    .append(String.format("👤 <b>Position:</b> %s (%s)\n",
                            nullToNA(userEntity.getPosition()), nullToNA(userEntity.getPosition())))
                    .append(String.format("👤 <b>Deleted By:</b> %s (%s)\n",
                            nullToNA(userEntity.getFullName()), nullToNA(userEntity.getEmail())))
                    .append(String.format("🕒 <b>Deleted At:</b> %s\n", formatToPhnomPenh(LocalDateTime.now())))
                    .append("\n─────────────────────────\n📌 <i>Sent via Internal Trainee Report System</i>");

            sendTelegramMessage(message.toString());
            log.info("Telegram notification sent for deleted trainee report ID: {}", reportId);
        } catch (Exception e) {
            log.error("Failed to send Telegram notification for trainee report deletion: {}", e.getMessage(), e);
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

    private String buildReportMessage(TraineeReport report, String type) {
        UserEntity userEntity = userRepository.findByUsername(report.getCreatedBy())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String icon = type.equals("Created") ? "🆕" : "✏️";

        StringBuilder message = new StringBuilder();
        message.append(String.format("%s <b>Trainee Report %s</b>\n", icon, type))
                .append("─────────────────────────\n")
                .append(String.format("🆔 <b>Report ID:</b> <code>INTERNAL#%d</code>\n", report.getId()))
                .append(String.format("👤 <b>Reported By:</b> %s (%s)\n",
                        nullToNA(userEntity.getFullName()), nullToNA(userEntity.getEmail())))
                .append(String.format("👤 <b>Position:</b> %s (%s)\n",
                        nullToNA(userEntity.getPosition()), nullToNA(userEntity.getPosition())))
                .append(String.format("📝 <b>%s By:</b> %s\n", type,
                        nullToNA(type.equals("Created") ? report.getCreatedBy() : report.getUpdatedBy())))
                .append(String.format("🕒 <b>%s At:</b> %s\n", type,
                        type.equals("Created")
                                ? formatToPhnomPenh(report.getCreatedAt())
                                : formatToPhnomPenh(report.getUpdatedAt())));

        // Optional fields
        message.append("\n📋 <b>Report Remark:</b>\n")
                .append(nullToNA(truncateText(report.getReportRemark(), 1000))).append("\n")
                .append("\n⚠️ <b>Challenges:</b>\n")
                .append(nullToNA(truncateText(report.getChallenge(), 1000))).append("\n")
                .append("\n💡 <b>Recommendations:</b>\n")
                .append(nullToNA(truncateText(report.getRecommend(), 1000))).append("\n")
                .append("\n─────────────────────────\n📌 <i>Sent via Internal Trainee Report System</i>");

        return message.toString();
    }

    /** Converts UTC LocalDateTime → Asia/Phnom_Penh (UTC+7) with 12-hour format */
    private String formatToPhnomPenh(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return "N/A";
        return utcDateTime
                .atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(PHNOM_PENH)
                .format(DATETIME_FORMATTER);
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.trim().isEmpty()) return "N/A";
        text = text.trim();
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private String nullToNA(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value.trim();
    }
}
