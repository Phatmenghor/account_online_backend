package com.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cpb")
public class CpbProperties {

    private String environment;
    private Camdx camdx;
    private Aml aml;
    private T24 t24;
    private Mb mb;
    private Otp otp;
    private Validate validate;
    private Ocr ocr;
    private Xml xml;

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
    public static class Aml {
        private String token;
        private String url;
        private boolean devForceHighRisk = false;
    }

    @Data
    public static class T24 {
        private String url;
        private String username;
        private String password;
    }

    @Data
    public static class Mb {
        private String otpUrl;
        private String registerCodeUrl;
        private String secretKey;
    }

    @Data
    public static class Otp {
        private int length;
        private int cooldownSeconds;
        private int maxAttempts;
        private int lockMinutes;
        private int expiryMinutes;
        private String message;
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
    public static class Xml {
        private String createCustomer;
        private String openAcctByCustomer;
        private String openAcctByStaff;
    }
}