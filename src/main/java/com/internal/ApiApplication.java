package com.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = "com.internal")
@Slf4j
public class ApiApplication {

    public static void main(String[] args) {
        log.info("Starting Account Online API...");
        ConfigurableApplicationContext context = SpringApplication.run(ApiApplication.class, args);

        String[] profiles = context.getEnvironment().getActiveProfiles();
        log.info("Active Spring Profiles: {}", String.join(", ", profiles));

        log.info("Account Online API started successfully");
    }
}
