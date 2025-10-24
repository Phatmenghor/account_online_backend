package com.internal.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class OracleDataSourceConfig {

    // ==================== DWH Database Configuration ====================

    @Bean(name = "dwhDataSourceProperties")
    @ConfigurationProperties("oracle.dwh.datasource")
    public DataSourceProperties dwhDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dwhDataSource")
    @ConfigurationProperties("oracle.dwh.datasource.hikari")
    public DataSource dwhDataSource(@Qualifier("dwhDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "dwhJdbcTemplate")
    public JdbcTemplate dwhJdbcTemplate(@Qualifier("dwhDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // ==================== STG Database Configuration ====================

    @Bean(name = "stgDataSourceProperties")
    @ConfigurationProperties("oracle.stg.datasource")
    public DataSourceProperties stgDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "stgDataSource")
    @ConfigurationProperties("oracle.stg.datasource.hikari")
    public DataSource stgDataSource(@Qualifier("stgDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "stgJdbcTemplate")
    public JdbcTemplate stgJdbcTemplate(@Qualifier("stgDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}