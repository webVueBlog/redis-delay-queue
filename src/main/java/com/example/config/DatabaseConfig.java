package com.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    // 主数据源配置
    @Value("${spring.datasource.primary.url}")
    private String primaryJdbcUrl;

    @Value("${spring.datasource.primary.username}")
    private String primaryUsername;

    @Value("${spring.datasource.primary.password}")
    private String primaryPassword;

    @Value("${spring.datasource.primary.driver-class-name}")
    private String primaryDriverClassName;

    // 用户数据源配置
    @Value("${spring.datasource.user.url}")
    private String userJdbcUrl;

    @Value("${spring.datasource.user.username}")
    private String userUsername;

    @Value("${spring.datasource.user.password}")
    private String userPassword;

    @Value("${spring.datasource.user.driver-class-name}")
    private String userDriverClassName;

    @Bean
    @Primary
    @Qualifier("primaryDataSource")
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(primaryJdbcUrl);
        config.setUsername(primaryUsername);
        config.setPassword(primaryPassword);
        config.setDriverClassName(primaryDriverClassName);
        
        // 连接池配置
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setLeakDetectionThreshold(60000);
        
        // 连接测试
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);
        
        // 连接池名称
        config.setPoolName("HikariPool-Primary");
        
        // 自动提交
        config.setAutoCommit(true);
        
        return new HikariDataSource(config);
    }

    @Bean
    @Qualifier("userDataSource")
    public DataSource userDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(userJdbcUrl);
        config.setUsername(userUsername);
        config.setPassword(userPassword);
        config.setDriverClassName(userDriverClassName);
        
        // 连接池配置
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setLeakDetectionThreshold(60000);
        
        // 连接测试
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);
        
        // 连接池名称
        config.setPoolName("HikariPool-User");
        
        // 自动提交
        config.setAutoCommit(true);
        
        return new HikariDataSource(config);
    }
}