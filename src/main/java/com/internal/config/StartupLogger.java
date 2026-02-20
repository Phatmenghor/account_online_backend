package com.internal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupLogger {

    private final Environment env;

    public StartupLogger(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        String port = env.getProperty("local.server.port", "8080");
        String[] profiles = env.getActiveProfiles();

        log.info("New Bro");
        log.info("============================================================");
        log.info("Account Online Application STARTED SUCCESSFULLY");
        log.info("------------------------------------------------------------");
        log.info("Active Profile(s): {}",
                profiles.length > 0 ? String.join(", ", profiles) : "default");
        log.info("");
        log.info("Access Points:");
        log.info("  Application : http://localhost:{}", port);
        log.info("  Swagger UI  : http://localhost:{}/swagger-ui.html", port);
        log.info("  Health Check: http://localhost:{}/actuator/health", port);
        log.info("============================================================");
        log.info("");
    }
}
