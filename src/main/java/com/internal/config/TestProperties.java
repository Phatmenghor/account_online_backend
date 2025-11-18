package com.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "test")
@Data
public class TestProperties {
    private boolean skipCheckAccount;
    private boolean skipCheckCif;
}
