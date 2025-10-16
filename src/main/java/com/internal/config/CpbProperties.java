package com.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cpb")
public class CpbProperties {

    private Camdx camdx;
    private Validate validate;
    private Ocr ocr;
    private Mb mb;
    private Otp otp;

    @Data
    public static class Camdx {
        private String url;
        private Auth auth;

        @Data
        public static class Auth {
            private String token;
            private String tokenHeaderName;
        }
    }

    @Data
    public static class Validate {
        private String nid;
        private String faceApiRoute;
    }

    @Data
    public static class Ocr {
        private String apiRoute;
    }

    @Data
    public static class Mb {
        private String otpUrl;
        private String registerCodeUrl;
        private String secretKey; // Add this to YAML as cpb.mb.secretKey
    }

    @Data
    public static class Otp {
        private int length;
        private int cooldownSeconds;
        private int maxAttempts;
        private int lockMinutes;
    }
}
