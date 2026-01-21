package com.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.internal")
@EnableScheduling
@EnableAsync
@Slf4j
public class ApiApplication {

    public static void main(String[] args) {
        log.info("Starting Account Online API...");

        ConfigurableApplicationContext context =
                SpringApplication.run(ApiApplication.class, args);

        Environment env = context.getEnvironment();
        String[] profiles = env.getActiveProfiles();
        String port = env.getProperty("server.port", "8080");

        log.info("Account Online Application started successfully");
        log.info("Active Spring Profiles: {}",
                profiles.length > 0 ? String.join(", ", profiles) : "default");

        log.info("🌐 Access Points:");
        log.info("• Application: http://localhost:{}", port);
        log.info("• Swagger UI: http://localhost:{}/swagger-ui.html", port);
        log.info("• Health Check: http://localhost:{}/actuator/health", port);
    }
}
