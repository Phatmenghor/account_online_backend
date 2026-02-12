package com.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cpb")
public class CpbProperties {

    private String environment;
    private Defaults defaults;
    private Camdx camdx;
    private Aml aml;
    private T24 t24;
    private Mb mb;
    private Otp otp;
    private Validate validate;
    private Ocr ocr;

    @Data
    public static class Defaults {
        private String branchCode;
        private String sector;
        private String costCenter;
        private String industry;
        private String target;
        private String language;
        private String customerRating;
        private String customerStatus;
        private String customerType;
        private String ownership;
        private String legalHolderName;
        private String nationality;
        private String productCode;
        private String accountActivity;
        private String newArrangement;
    }

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
        private String url;
        private String username;
        private String password;
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
}