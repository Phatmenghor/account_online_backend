package com.internal.feature.telegram_alerts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.uat-acl-chat-id}")
    private String chatId_acl_internal;

    @Value("${telegram.bot.uat-monitor-chat-id}")
    private String chatId_uat_monitor;

    @Value("${telegram.bot.uat-dev-team-chat-id:}")
    private String chatId_dev_team;

    private final RestTemplate restTemplate = new RestTemplate();
    private final TaskExecutor taskExecutor;

    public TelegramService(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void sendMarkdownAclInternalMessage(String message) {
        taskExecutor.execute(() -> sendMarkdownToChat(chatId_acl_internal, message));
    }

    public void sendMarkdownAccountOnlineMonitorMessage(String message) {
        taskExecutor.execute(() -> sendMarkdownToChat(chatId_uat_monitor, message));
    }

    public void sendDetailedErrorToDevTeam(String message) {
        taskExecutor.execute(() -> {
            if (chatId_dev_team == null || chatId_dev_team.trim().isEmpty()) {
                log.debug("Dev Team chat ID not configured - skipping Telegram alert");
                return;
            }
            sendMarkdownToChat(chatId_dev_team, message);
        });
    }

    public void sendCriticalErrorAlert(String title, String details) {
        taskExecutor.execute(() -> {
            String message = String.format(
                    "*🚨 %s*\n%s",
                    title, details);
            sendMarkdownToChat(chatId_dev_team, message);
        });
    }

    public void sendMarkdownToChat(String chatId, String message) {
        if (chatId == null || chatId.trim().isEmpty()) {
            return;
        }
        try {
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId);
            body.add("text", message);
            body.add("parse_mode", "Markdown");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, requestEntity, String.class);
            log.debug("Telegram message sent successfully");
        } catch (Exception e) {
            log.warn("Telegram send failed - chat_id: {}, error: {}", chatId, e.getMessage());
        }
    }

    public void sendPhoto(String chatId, String caption, Resource imageResource) {
        if (chatId == null || chatId.trim().isEmpty()) {
            return;
        }
        taskExecutor.execute(() -> {
            try {
                String url = String.format("https://api.telegram.org/bot%s/sendPhoto", botToken);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("chat_id", chatId);
                body.add("caption", caption);
                body.add("photo", imageResource);
                body.add("parse_mode", "Markdown");

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                restTemplate.postForObject(url, requestEntity, String.class);
            } catch (Exception e) {
                log.warn("Failed to send Telegram photo: {}", e.getMessage());
            }
        });
    }
}