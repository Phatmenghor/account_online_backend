package com.internal.feature.telegram_alerts.config;

import org.springframework.beans.factory.annotation.Value;
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

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMarkdownAclInternalMessage(String message) {
        try {
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId_acl_internal);
            body.add("text", message);
            body.add("parse_mode", "Markdown");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, requestEntity, String.class);
            log.info("Telegram sent to ACL Internal channel.");
        } catch (Exception e) {
            log.error("Failed to send Telegram to ACL Internal channel: {}", e.getMessage(), e);
        }
    }

    public void sendMarkdownAccountOnlineMonitorMessage(String message) {
        try {
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatId_uat_monitor);
            body.add("text", message);
            body.add("parse_mode", "Markdown");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, requestEntity, String.class);
            log.info("Telegram sent to Monitor channel.");
        } catch (Exception e) {
            log.error("Failed to send Telegram to Monitor channel: {}", e.getMessage(), e);
        }
    }

    public void sendMarkdownToChat(String chatId, String message) {
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
            log.info("Telegram sent to chat: {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send Telegram to chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    public void sendPhoto(String chatId, String caption, Resource imageResource) {
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
            log.info("Telegram photo sent to chat: {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send Telegram photo to chat {}: {}", chatId, e.getMessage(), e);
        }
    }
}