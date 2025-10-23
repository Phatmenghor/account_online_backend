package com.internal.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

@Configuration
public class OracleDataSourceConfig {

    // ==================== DWH Database Configuration ====================

    @Bean
    @Primary
    @ConfigurationProperties("oracle.dwh.datasource")
    public DataSourceProperties dwhDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("oracle.dwh.datasource.hikari")
    public HikariDataSource dwhDataSource() {
        return dwhDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    @Qualifier("dwhJdbcTemplate")
    public JdbcTemplate dwhJdbcTemplate(@Qualifier("dwhDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    // ==================== STG Database Configuration ====================

    @Bean
    @ConfigurationProperties("oracle.stg.datasource")
    public DataSourceProperties stgDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("oracle.stg.datasource.hikari")
    public HikariDataSource stgDataSource() {
        return stgDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("stgJdbcTemplate")
    public JdbcTemplate stgJdbcTemplate(@Qualifier("stgDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
