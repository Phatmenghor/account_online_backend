package com.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@Data
public class TelegramConfig {
    private String token;
    private String chatId;
    private boolean enabled = true;
}