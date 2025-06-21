package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * 系统设置数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDTO {
    
    /**
     * 基础设置
     */
    @JsonProperty("basicSettings")
    private BasicSettings basicSettings;
    
    /**
     * Redis配置
     */
    @JsonProperty("redisSettings")
    private RedisSettings redisSettings;
    
    /**
     * 队列设置
     */
    @JsonProperty("queueSettings")
    private QueueSettings queueSettings;
    
    /**
     * 安全设置
     */
    @JsonProperty("securitySettings")
    private SecuritySettings securitySettings;
    
    /**
     * 通知设置
     */
    @JsonProperty("notificationSettings")
    private NotificationSettings notificationSettings;
    
    /**
     * 性能优化设置
     */
    @JsonProperty("performanceSettings")
    private PerformanceSettings performanceSettings;
    
    /**
     * 基础设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicSettings {
        private String systemName;
        private String systemDescription;
        private String systemVersion;
        private String adminEmail;
        private String timezone;
        private String logLevel;
    }
    
    /**
     * Redis配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisSettings {
        private String host;
        private Integer port;
        private Integer database;
        private String password;
        private Integer timeout;
        private Integer maxActive;
        private Integer maxIdle;
        private Integer minIdle;
    }
    
    /**
     * 队列设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueSettings {
        private String defaultQueueName;
        private Integer maxRetryCount;
        private Integer taskTimeout;
        private Integer batchSize;
        private Integer scanInterval;
        private Boolean enablePriority;
    }
    
    /**
     * 安全设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecuritySettings {
        private String jwtSecret;
        private Integer tokenExpiration;
        private Integer passwordMinLength;
        private Integer loginFailureLockCount;
        private Boolean enableTwoFactor;
        private String ipWhitelist;
    }
    
    /**
     * 通知设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettings {
        private Boolean enableEmailNotification;
        private String smtpHost;
        private Integer smtpPort;
        private String smtpUsername;
        private String smtpPassword;
        private Boolean enableWebhookNotification;
        private String webhookUrl;
        private String notificationEvents;
    }
    
    /**
     * 性能优化设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceSettings {
        private Integer threadPoolSize;
        private Integer queueCapacity;
        private Boolean enableCache;
        private Boolean enableCompression;
        private Integer connectionPoolSize;
        private Integer maxConcurrentRequests;
        private Integer requestTimeout;
    }
    
    /**
     * 转换为Map格式（用于批量保存）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        // 基础设置
        if (basicSettings != null) {
            map.put("basic.systemName", basicSettings.getSystemName());
            map.put("basic.systemDescription", basicSettings.getSystemDescription());
            map.put("basic.systemVersion", basicSettings.getSystemVersion());
            map.put("basic.adminEmail", basicSettings.getAdminEmail());
            map.put("basic.timezone", basicSettings.getTimezone());
            map.put("basic.logLevel", basicSettings.getLogLevel());
        }
        
        // Redis配置
        if (redisSettings != null) {
            map.put("redis.host", redisSettings.getHost());
            map.put("redis.port", redisSettings.getPort());
            map.put("redis.database", redisSettings.getDatabase());
            map.put("redis.password", redisSettings.getPassword());
            map.put("redis.timeout", redisSettings.getTimeout());
            map.put("redis.maxActive", redisSettings.getMaxActive());
            map.put("redis.maxIdle", redisSettings.getMaxIdle());
            map.put("redis.minIdle", redisSettings.getMinIdle());
        }
        
        // 队列设置
        if (queueSettings != null) {
            map.put("queue.defaultQueueName", queueSettings.getDefaultQueueName());
            map.put("queue.maxRetryCount", queueSettings.getMaxRetryCount());
            map.put("queue.taskTimeout", queueSettings.getTaskTimeout());
            map.put("queue.batchSize", queueSettings.getBatchSize());
            map.put("queue.scanInterval", queueSettings.getScanInterval());
            map.put("queue.enablePriority", queueSettings.getEnablePriority());
        }
        
        // 安全设置
        if (securitySettings != null) {
            map.put("security.jwtSecret", securitySettings.getJwtSecret());
            map.put("security.tokenExpiration", securitySettings.getTokenExpiration());
            map.put("security.passwordMinLength", securitySettings.getPasswordMinLength());
            map.put("security.loginFailureLockCount", securitySettings.getLoginFailureLockCount());
            map.put("security.enableTwoFactor", securitySettings.getEnableTwoFactor());
            map.put("security.ipWhitelist", securitySettings.getIpWhitelist());
        }
        
        // 通知设置
        if (notificationSettings != null) {
            map.put("notification.enableEmailNotification", notificationSettings.getEnableEmailNotification());
            map.put("notification.smtpHost", notificationSettings.getSmtpHost());
            map.put("notification.smtpPort", notificationSettings.getSmtpPort());
            map.put("notification.smtpUsername", notificationSettings.getSmtpUsername());
            map.put("notification.smtpPassword", notificationSettings.getSmtpPassword());
            map.put("notification.enableWebhookNotification", notificationSettings.getEnableWebhookNotification());
            map.put("notification.webhookUrl", notificationSettings.getWebhookUrl());
            map.put("notification.notificationEvents", notificationSettings.getNotificationEvents());
        }
        
        // 性能优化设置
        if (performanceSettings != null) {
            map.put("performance.threadPoolSize", performanceSettings.getThreadPoolSize());
            map.put("performance.queueCapacity", performanceSettings.getQueueCapacity());
            map.put("performance.enableCache", performanceSettings.getEnableCache());
            map.put("performance.enableCompression", performanceSettings.getEnableCompression());
            map.put("performance.connectionPoolSize", performanceSettings.getConnectionPoolSize());
            map.put("performance.maxConcurrentRequests", performanceSettings.getMaxConcurrentRequests());
            map.put("performance.requestTimeout", performanceSettings.getRequestTimeout());
        }
        
        return map;
    }
}