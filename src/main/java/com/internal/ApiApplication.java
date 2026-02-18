package com.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.internal")
@EnableScheduling
@EnableAsync
@Slf4j
public class ApiApplication {

    @javax.annotation.PostConstruct
    public void init() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Bangkok"));
        log.info("Application TimeZone set to Asia/Bangkok (UTC+7)");
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
