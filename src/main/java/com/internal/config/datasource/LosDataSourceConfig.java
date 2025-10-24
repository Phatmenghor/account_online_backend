//package com.internal.config.datasource;
//
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class LosDataSourceConfig {
//
//    @Bean(name = "losDataSourceProperties")
//    @ConfigurationProperties("spring.los.datasource")
//    public DataSourceProperties losDataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    @Bean(name = "losDataSource")
//    @ConfigurationProperties("spring.los.datasource.hikari")
//    public HikariDataSource losDataSource(@Qualifier("losDataSourceProperties") DataSourceProperties properties) {
//        return properties.initializeDataSourceBuilder()
//                .type(HikariDataSource.class)
//                .build();
//    }
////
////    @Bean(name = "losJdbcTemplate")
////    public JdbcTemplate losJdbcTemplate(@Qualifier("losDataSource") DataSource ds) {
////        return new JdbcTemplate(ds);
////    }
//}
