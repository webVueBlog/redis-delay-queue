package com.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Properties;

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

    // HikariCP 连接池配置参数
    @Value("${spring.datasource.hikari.maximum-pool-size:30}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:10}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.validation-timeout:5000}")
    private long validationTimeout;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Value("${spring.datasource.hikari.connection-test-query:SELECT 1}")
    private String connectionTestQuery;

    @Value("${spring.datasource.hikari.connection-init-sql:SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci}")
    private String connectionInitSql;

    @Value("${spring.datasource.hikari.register-mbeans:true}")
    private boolean registerMbeans;

    @Bean
    @Primary
    @Qualifier("primaryDataSource")
    public DataSource primaryDataSource() {
        return createOptimizedDataSource(
            primaryJdbcUrl, 
            primaryUsername, 
            primaryPassword, 
            primaryDriverClassName, 
            "HikariPool-Primary"
        );
    }

    /**
     * 创建优化的数据源配置
     * @param jdbcUrl 数据库连接URL
     * @param username 用户名
     * @param password 密码
     * @param driverClassName 驱动类名
     * @param poolName 连接池名称
     * @return 配置好的数据源
     */
    private DataSource createOptimizedDataSource(String jdbcUrl, String username, 
                                                String password, String driverClassName, 
                                                String poolName) {
        HikariConfig config = new HikariConfig();
        
        // 基础连接配置
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // 连接池大小配置
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        
        // 连接超时配置
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setValidationTimeout(validationTimeout);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // 连接测试配置
        config.setConnectionTestQuery(connectionTestQuery);
        config.setConnectionInitSql(connectionInitSql);
        
        // 连接池名称和监控
        config.setPoolName(poolName);
        // 禁用MBean注册以避免重复注册异常
        config.setRegisterMbeans(false);
        
        // 事务配置
        config.setAutoCommit(true);
        config.setReadOnly(false);
        
        // MySQL性能优化配置
        Properties dataSourceProperties = new Properties();
        dataSourceProperties.setProperty("cachePrepStmts", "true");
        dataSourceProperties.setProperty("prepStmtCacheSize", "250");
        dataSourceProperties.setProperty("prepStmtCacheSqlLimit", "2048");
        dataSourceProperties.setProperty("useServerPrepStmts", "true");
        dataSourceProperties.setProperty("useLocalSessionState", "true");
        dataSourceProperties.setProperty("rewriteBatchedStatements", "true");
        dataSourceProperties.setProperty("cacheResultSetMetadata", "true");
        dataSourceProperties.setProperty("cacheServerConfiguration", "true");
        dataSourceProperties.setProperty("elideSetAutoCommits", "true");
        dataSourceProperties.setProperty("maintainTimeStats", "false");
        config.setDataSourceProperties(dataSourceProperties);
        
        return new HikariDataSource(config);
    }

    @Bean
    @Qualifier("userDataSource")
    public DataSource userDataSource() {
        return createOptimizedDataSource(
            userJdbcUrl, 
            userUsername, 
            userPassword, 
            userDriverClassName, 
            "HikariPool-User"
        );
    }

    @Bean
    @Qualifier("userJdbcTemplate")
    public JdbcTemplate userJdbcTemplate(@Qualifier("userDataSource") DataSource userDataSource) {
        return new JdbcTemplate(userDataSource);
    }
}