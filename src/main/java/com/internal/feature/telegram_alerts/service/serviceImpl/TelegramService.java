package com.internal.feature.telegram_alerts.service.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMarkdownMessage(String message) {
        try {
            String encodedMessage = URLEncoder.encode(message, String.valueOf(StandardCharsets.UTF_8));
            String url = String.format(
                    "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&parse_mode=Markdown&text=%s",
                    botToken, chatId, encodedMessage
            );
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        sendMarkdownMessage(message);
    }
}
