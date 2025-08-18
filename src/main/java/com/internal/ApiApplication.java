package com.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = "com.internal")
@Slf4j
public class ApiApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Phnom_Penh"));
        log.info("Application timezone set to: {}", TimeZone.getDefault().getID());
    }

    public static void main(String[] args) {
        log.info("Starting Internal CBC API Application...");
        SpringApplication.run(ApiApplication.class, args);
        log.info("Internal CBC API Application started successfully");
    }
}
