package com.internal.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableConfigurationProperties
public class OracleDataSourceConfig {

    // ==================== DWH Database Configuration ====================

    @Bean(name = "dwhHikariConfig")
    @ConfigurationProperties(prefix = "oracle.dwh.datasource")
    public HikariConfig dwhHikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = "dwhDataSource")
    public DataSource dwhDataSource(@Qualifier("dwhHikariConfig") HikariConfig config) {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().isEmpty()) {
            log.error("DWH Oracle JDBC URL is NULL or EMPTY!");
            log.error("Check your application.yaml - oracle.dwh.datasource.jdbc-url");
            throw new IllegalStateException("DWH Oracle JDBC URL not configured!");
        }

        log.info("✔ Creating DWH Oracle DataSource");
        log.info("   URL: {}", config.getJdbcUrl());
        log.info("   Username: {}", config.getUsername());
        log.info("   Driver: {}", config.getDriverClassName());
        log.info("   Pool: {}", config.getPoolName());

        return new HikariDataSource(config);
    }

    @Bean(name = "dwhJdbcTemplate")
    public JdbcTemplate dwhJdbcTemplate(@Qualifier("dwhDataSource") DataSource dataSource) {
        log.info("✔ DWH Oracle JdbcTemplate created");
        return new JdbcTemplate(dataSource);
    }

    // ==================== STG Database Configuration ====================

    @Bean(name = "stgHikariConfig")
    @ConfigurationProperties(prefix = "oracle.stg.datasource")
    public HikariConfig stgHikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = "stgDataSource")
    public DataSource stgDataSource(@Qualifier("stgHikariConfig") HikariConfig config) {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().isEmpty()) {
            log.error("✖ STG Oracle JDBC URL is NULL or EMPTY!");
            log.error("Check your application.yaml - oracle.stg.datasource.jdbc-url");
            throw new IllegalStateException("STG Oracle JDBC URL not configured!");
        }

        log.info("✔ Creating STG Oracle DataSource");
        log.info("   URL: {}", config.getJdbcUrl());
        log.info("   Username: {}", config.getUsername());
        log.info("   Driver: {}", config.getDriverClassName());
        log.info("   Pool: {}", config.getPoolName());

        return new HikariDataSource(config);
    }

    @Bean(name = "stgJdbcTemplate")
    public JdbcTemplate stgJdbcTemplate(@Qualifier("stgDataSource") DataSource dataSource) {
        log.info("✔ STG Oracle JdbcTemplate created");
        return new JdbcTemplate(dataSource);
    }
}
