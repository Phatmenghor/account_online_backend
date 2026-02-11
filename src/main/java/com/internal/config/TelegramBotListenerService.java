package com.internal.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.repository.AmlStatusRepository;
import com.internal.feature.logs_report.repository.AccountOnlineFinalRepository;
import com.internal.feature.telegram_alerts.config.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotListenerService {

    private final AccountOnlineFinalRepository accountOnlineFinalRepository;
    private final AmlStatusRepository amlStatusRepository;
    private final TelegramService telegramService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final ZoneId ZONE_PP = ZoneId.of("Asia/Phnom_Penh");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.enabled:true}")
    private boolean enabled;

    @Value("${aml.dashboard.url:}")
    private String amlDashboardUrl;

    private long lastUpdateId = 0;

    // =====================================================
    // POLL EVERY 3 SECONDS
    // =====================================================
    @Scheduled(fixedDelay = 3000)
    public void pollMessages() {

        if (!enabled) return;

        try {
            String url = String.format(
                    "https://api.telegram.org/bot%s/getUpdates?offset=%d&timeout=2",
                    botToken, lastUpdateId + 1
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (!root.path("ok").asBoolean()) return;

            JsonNode results = root.path("result");

            for (JsonNode update : results) {
                lastUpdateId = update.path("update_id").asLong();

                JsonNode message = update.path("message");
                if (message.isMissingNode()) continue;

                String text = message.path("text").asText("").toLowerCase().trim();
                long chatId = message.path("chat").path("id").asLong();
                String senderName = message.path("from").path("first_name").asText("Unknown");

                if (text.isEmpty()) continue;

                log.info("Bot received from {}: {}", senderName, text);

                handleMessage(chatId, text, senderName);
            }

        } catch (Exception e) {
            log.error("Error polling Telegram: {}", e.getMessage());
        }
    }

    // =====================================================
    // HANDLE MESSAGES
    // =====================================================
    private void handleMessage(long chatId, String text, String senderName) {

        if (containsAny(text, "how many success", "success open", "open account", "how many open")) {
            sendAccountReport(chatId, senderName);

        } else if (containsAny(text, "how many pending", "aml pending", "pending aml", "pending case")) {
            sendAmlPendingReport(chatId, senderName);

        } else if (containsAny(text, "today report", "report today", "daily report", "send report")) {
            sendAccountReport(chatId, senderName);
            sendAmlPendingReport(chatId, senderName);

        } else if (containsAny(text, "help", "what can you do", "command")) {
            sendHelpMessage(chatId, senderName);

        } else if (containsAny(text, "hi bot", "hello bot", "hey bot", "hi bro bot")) {
            sendGreeting(chatId, senderName);
        }
    }

    // =====================================================
    // ACCOUNT REPORT
    // =====================================================
    private void sendAccountReport(long chatId, String senderName) {

        try {
            LocalDate today = LocalDate.now(ZONE_PP);
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            List<Object[]> results = accountOnlineFinalRepository
                    .countByGenderAndDateRange(startOfDay, endOfDay);

            long maleCount = 0;
            long femaleCount = 0;
            long otherCount = 0;

            for (Object[] row : results) {
                String gender = (String) row[0];
                long count = (Long) row[1];

                if ("MALE".equalsIgnoreCase(gender)) {
                    maleCount = count;
                } else if ("FEMALE".equalsIgnoreCase(gender)) {
                    femaleCount = count;
                } else {
                    otherCount += count;
                }
            }

            long totalCount = maleCount + femaleCount + otherCount;

            String reportDate = today.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            String generatedAt = LocalDateTime.now(ZONE_PP).format(FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("Hi ").append(escapeMarkdown(senderName)).append("!\n\n")
              .append("*TODAY ACCOUNT OPENING REPORT*\n")
              .append("--------------------\n")
              .append("Report Date: *").append(reportDate).append("*\n\n")
              .append("Male: *").append(maleCount).append("*\n")
              .append("Female: *").append(femaleCount).append("*\n");

            if (otherCount > 0) {
                sb.append("Other: *").append(otherCount).append("*\n");
            }

            sb.append("\nTotal: *").append(totalCount).append("*\n")
              .append("--------------------\n")
              .append("Generated: ").append(generatedAt);

            telegramService.sendMarkdownToChat(String.valueOf(chatId), sb.toString());

        } catch (Exception e) {
            log.error("Error sending account report: {}", e.getMessage());
            telegramService.sendMarkdownToChat(String.valueOf(chatId),
                    "Sorry, failed to get account report\\. Please check logs\\.");
        }
    }

    // =====================================================
    // AML PENDING REPORT
    // =====================================================
    private void sendAmlPendingReport(long chatId, String senderName) {

        try {
            long pendingCount = amlStatusRepository.countByStatus(AmlStatusEnum.PENDING);

            String generatedAt = LocalDateTime.now(ZONE_PP).format(FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("Hi ").append(escapeMarkdown(senderName)).append("!\n\n")
              .append("*AML PENDING REPORT*\n")
              .append("--------------------\n\n")
              .append("Total Pending: *").append(pendingCount).append("*\n\n");

            if (pendingCount > 0) {
                sb.append("There are *").append(pendingCount)
                  .append("* customer(s) awaiting AML review\\.\n\n");

                if (amlDashboardUrl != null && !amlDashboardUrl.isEmpty()) {
                    sb.append("Dashboard:\n").append(amlDashboardUrl).append("\n");
                }
            } else {
                sb.append("No pending AML cases\\. All clear\\.\n");
            }

            sb.append("--------------------\n")
              .append("Generated: ").append(generatedAt);

            telegramService.sendMarkdownToChat(String.valueOf(chatId), sb.toString());

        } catch (Exception e) {
            log.error("Error sending AML report: {}", e.getMessage());
            telegramService.sendMarkdownToChat(String.valueOf(chatId),
                    "Sorry, failed to get AML report\\. Please check logs\\.");
        }
    }

    // =====================================================
    // HELP
    // =====================================================
    private void sendHelpMessage(long chatId, String senderName) {

        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(escapeMarkdown(senderName)).append("!\n\n")
          .append("*BOT COMMANDS*\n")
          .append("--------------------\n\n")
          .append("You can ask me:\n\n")
          .append("- How many success open?\n")
          .append("- How many pending AML?\n")
          .append("- Send report today\n")
          .append("- Help\n\n")
          .append("--------------------\n")
          .append("Account Online Bot");

        telegramService.sendMarkdownToChat(String.valueOf(chatId), sb.toString());
    }

    // =====================================================
    // GREETING
    // =====================================================
    private void sendGreeting(long chatId, String senderName) {

        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(escapeMarkdown(senderName))
          .append("! I'm Account Online Bot\\.\n\n")
          .append("Type *help* to see what I can do\\.");

        telegramService.sendMarkdownToChat(String.valueOf(chatId), sb.toString());
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }
}