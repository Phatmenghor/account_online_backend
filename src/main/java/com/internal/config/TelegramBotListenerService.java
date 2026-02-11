package com.internal.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internal.feature.auth.repository.UserRepository;
import com.internal.feature.telegram_alerts.config.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotListenerService {

    private final TelegramService telegramService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final ZoneId ZONE_PP = ZoneId.of("Asia/Phnom_Penh");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final String DEFAULT_PASSWORD = "88889999"; // Change as needed

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.enabled:true}")
    private boolean enabled;

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

        if (containsAny(text, "reset password", "reset pass")) {
            String email = extractEmail(text);
            handleResetPassword(chatId, senderName, email);

        } else if (containsAny(text, "hi bot", "hello bot", "hey bot", "help")) {
            sendGreeting(chatId, senderName);
        }
    }

    // =====================================================
    // GREETING
    // =====================================================
    private void sendGreeting(long chatId, String senderName) {

        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(escapeMarkdown(senderName)).append("!\n\n")
                .append("How can I help you?");

        telegramService.sendMarkdownToChat(String.valueOf(chatId), sb.toString());
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================
    private void handleResetPassword(long chatId, String senderName, String username) {

        if (username == null || username.isEmpty()) {
            telegramService.sendMarkdownToChat(String.valueOf(chatId),
                    "Hi " + escapeMarkdown(senderName) + "!\n\n" +
                            "I couldn't find an email in your message\\.\n\n" +
                            "Example:\n" +
                            "`reset password phatmenghor19@gmail.com`");
            return;
        }

        try {
            var userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                telegramService.sendMarkdownToChat(String.valueOf(chatId),
                        "Hi " + escapeMarkdown(senderName) + "!\n\n" +
                                "User *" + escapeMarkdown(username) + "* not found\\.");
                return;
            }

            var user = userOptional.get();
            user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            userRepository.save(user);

            String generatedAt = LocalDateTime.now(ZONE_PP).format(FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("Hi ").append(escapeMarkdown(senderName)).append("!\n\n")
                    .append("*PASSWORD RESET SUCCESS*\n")
                    .append("--------------------\n\n")
                    .append("User: *").append(escapeMarkdown(username)).append("*\n")
                    .append("Password reset to default\\.\n\n")
                    .append("--------------------\n")
                    .append("Reset by: ").append(escapeMarkdown(senderName)).append("\n")
                    .append("Time: ").append(generatedAt);

            telegramService.sendMarkdownToChat(String.valueOf(chatId), sb.toString());

            log.info("Password reset by {} for user: {}", senderName, username);

        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage());
            telegramService.sendMarkdownToChat(String.valueOf(chatId),
                    "Failed to reset password\\. Please check logs\\.");
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

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