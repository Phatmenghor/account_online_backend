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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraineeReportTelegramNotificationServiceImpl implements TraineeReportTelegramNotificationService {

    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                    .withZone(ZoneId.of("UTC"));

    @Override
    @Async
    public void sendReportCreatedNotification(TraineeReport report) {
        if (!telegramConfig.isEnabled()) {
            log.debug("Telegram notification is disabled");
            return;
        }

        try {
            String message = buildReportCreatedMessage(report);
            sendTelegramMessage(message);
            log.info("Telegram notification sent for new trainee report ID: {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram notification for trainee report creation: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendReportUpdatedNotification(TraineeReport report) {
        if (!telegramConfig.isEnabled()) {
            log.debug("Telegram notification is disabled");
            return;
        }

        try {
            String message = buildReportUpdatedMessage(report);
            sendTelegramMessage(message);
            log.info("Telegram notification sent for updated trainee report ID: {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram notification for trainee report update: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendReportDeletedNotification(Long reportId, String deletedBy) {
        if (!telegramConfig.isEnabled()) {
            log.debug("Telegram notification is disabled");
            return;
        }

        try {
            String message = buildReportDeletedMessage(reportId, deletedBy);
            sendTelegramMessage(message);
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

    private String buildReportCreatedMessage(TraineeReport report) {

        StringBuilder message = new StringBuilder();

        message.append("📝 <b>New Trainee Report Created</b>\n\n");
        message.append(String.format("🆔 <b>Report ID:</b> #%d\n", report.getId()));
        message.append(String.format(" <b>Report By:</b> %s\n", report.getReportBy()));

        if (report.getCreatedBy() != null && !report.getCreatedBy().trim().isEmpty()) {
            message.append(String.format("👤 <b>Created By:</b> %s\n", report.getCreatedBy()));
        }
        
        if (report.getCreatedAt() != null) {
            message.append(String.format("🕐 <b>Created At:</b> %s\n\n", 
                    report.getCreatedAt().format(DATETIME_FORMATTER)));
        }
        
        if (report.getReportRemark() != null && !report.getReportRemark().trim().isEmpty()) {
            message.append("📋 <b>Report Remark:</b>\n");
            message.append(truncateText(report.getReportRemark(), 1000));
            message.append("\n\n");
        }
        
        if (report.getChallenge() != null && !report.getChallenge().trim().isEmpty()) {
            message.append("⚠️ <b>Challenges:</b>\n");
            message.append(truncateText(report.getChallenge(), 1000));
            message.append("\n\n");
        }
        
        if (report.getRecommend() != null && !report.getRecommend().trim().isEmpty()) {
            message.append("💡 <b>Recommendations:</b>\n");
            message.append(truncateText(report.getRecommend(), 1000));
        }

        return message.toString();
    }

    private String buildReportUpdatedMessage(TraineeReport report) {

        StringBuilder message = new StringBuilder();
        message.append("✏️ <b>Trainee Report Updated</b>\n\n");
        message.append(String.format("🆔 <b>Report ID:</b> #%d\n", report.getId()));
        message.append(String.format(" <b>Report By:</b> %s\n", report.getReportBy()));

        if (report.getUpdatedBy() != null && !report.getUpdatedBy().trim().isEmpty()) {
            message.append(String.format("👤 <b>Updated By:</b> %s\n", report.getUpdatedBy()));
        }
        
        if (report.getUpdatedAt() != null) {
            message.append(String.format("🕐 <b>Updated At:</b> %s\n\n", 
                    report.getUpdatedAt().format(DATETIME_FORMATTER)));
        }
        
        if (report.getReportRemark() != null && !report.getReportRemark().trim().isEmpty()) {
            message.append("📋 <b>Report Remark:</b>\n");
            message.append(truncateText(report.getReportRemark(), 1000));
            message.append("\n\n");
        }
        
        if (report.getChallenge() != null && !report.getChallenge().trim().isEmpty()) {
            message.append("⚠️ <b>Challenges:</b>\n");
            message.append(truncateText(report.getChallenge(), 1000));
            message.append("\n\n");
        }
        
        if (report.getRecommend() != null && !report.getRecommend().trim().isEmpty()) {
            message.append("💡 <b>Recommendations:</b>\n");
            message.append(truncateText(report.getRecommend(), 1000));
        }

        return message.toString();
    }

    private String buildReportDeletedMessage(Long reportId, String deletedBy) {

        StringBuilder message = new StringBuilder();
        message.append("🗑️ <b>Trainee Report Deleted</b>\n\n");
        message.append(String.format("🆔 <b>Report ID:</b> #%d\n", reportId));

        if (deletedBy != null && !deletedBy.trim().isEmpty()) {
            message.append(String.format("👤 <b>Deleted By:</b> %s\n", deletedBy));
        }
        
        message.append(String.format("🕐 <b>Deleted At:</b> %s\n", 
                java.time.LocalDateTime.now().format(DATETIME_FORMATTER)));

        return message.toString();
    }

    /**
     * Truncate text to specified length and add ellipsis if needed
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        
        text = text.trim();
        
        if (text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength) + "...";
    }
}