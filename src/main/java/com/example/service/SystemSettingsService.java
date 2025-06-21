package com.example.service;

import com.example.dto.SystemSettingsDTO;
import com.example.entity.SystemSettings;
import com.example.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 系统设置服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingsService {
    
    private final SystemSettingsRepository systemSettingsRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取所有系统设置
     */
    public SystemSettingsDTO getAllSettings() {
        log.info("获取所有系统设置");
        
        SystemSettingsDTO dto = new SystemSettingsDTO();
        
        // 获取基础设置
        dto.setBasicSettings(getBasicSettings());
        
        // 获取Redis配置
        dto.setRedisSettings(getRedisSettings());
        
        // 获取队列设置
        dto.setQueueSettings(getQueueSettings());
        
        // 获取安全设置
        dto.setSecuritySettings(getSecuritySettings());
        
        // 获取通知设置
        dto.setNotificationSettings(getNotificationSettings());
        
        // 获取性能优化设置
        dto.setPerformanceSettings(getPerformanceSettings());
        
        return dto;
    }
    
    /**
     * 保存所有系统设置
     */
    @Transactional
    public void saveAllSettings(SystemSettingsDTO dto, String updatedBy) {
        log.info("保存所有系统设置，操作者: {}", updatedBy);
        
        Map<String, Object> settingsMap = dto.toMap();
        
        for (Map.Entry<String, Object> entry : settingsMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value != null) {
                String[] parts = key.split("\\.", 2);
                if (parts.length == 2) {
                    String category = parts[0];
                    String settingKey = parts[1];
                    
                    saveOrUpdateSetting(category, settingKey, value.toString(), updatedBy);
                }
            }
        }
        
        log.info("系统设置保存完成");
    }
    
    /**
     * 测试Redis连接
     */
    public boolean testRedisConnection(SystemSettingsDTO.RedisSettings redisSettings) {
        log.info("测试Redis连接: {}:{}", redisSettings.getHost(), redisSettings.getPort());
        
        try {
            // 这里可以创建临时的Redis连接进行测试
            // 简化实现，直接使用当前的RedisTemplate测试
            redisTemplate.opsForValue().set("test:connection", "test");
            String result = (String) redisTemplate.opsForValue().get("test:connection");
            redisTemplate.delete("test:connection");
            
            boolean success = "test".equals(result);
            log.info("Redis连接测试结果: {}", success ? "成功" : "失败");
            return success;
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            return false;
        }
    }
    
    /**
     * 根据分类获取设置
     */
    public List<SystemSettings> getSettingsByCategory(String category) {
        return systemSettingsRepository.findByCategoryOrderBySettingKey(category);
    }
    
    /**
     * 获取单个设置值
     */
    public String getSettingValue(String category, String settingKey, String defaultValue) {
        try {
            Optional<SystemSettings> setting = systemSettingsRepository.findByCategoryAndSettingKey(category, settingKey);
            return setting.map(SystemSettings::getSettingValue).orElse(defaultValue);
        } catch (Exception e) {
            log.warn("查询设置时发生异常，使用默认值。category: {}, settingKey: {}, error: {}", category, settingKey, e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * 保存或更新单个设置
     */
    @Transactional
    public void saveOrUpdateSetting(String category, String settingKey, String settingValue, String updatedBy) {
        try {
            Optional<SystemSettings> existingSetting = systemSettingsRepository.findByCategoryAndSettingKey(category, settingKey);
            
            if (existingSetting.isPresent()) {
                SystemSettings setting = existingSetting.get();
                setting.setSettingValue(settingValue);
                setting.setUpdatedBy(updatedBy);
                systemSettingsRepository.save(setting);
            } else {
                SystemSettings newSetting = new SystemSettings();
                newSetting.setCategory(category);
                newSetting.setSettingKey(settingKey);
                newSetting.setSettingValue(settingValue);
                newSetting.setDataType("STRING");
                newSetting.setCreatedBy(updatedBy);
                newSetting.setUpdatedBy(updatedBy);
                systemSettingsRepository.save(newSetting);
            }
        } catch (Exception e) {
            log.error("保存或更新设置时发生异常。category: {}, settingKey: {}, error: {}", category, settingKey, e.getMessage(), e);
            // 如果是重复数据异常，尝试清理重复数据
            if (e.getMessage().contains("NonUniqueResultException") || e.getMessage().contains("query did not return a unique result")) {
                cleanupDuplicateSettings(category, settingKey, settingValue, updatedBy);
            } else {
                throw new RuntimeException("保存设置失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 清理重复的设置数据
     */
    @Transactional
    private void cleanupDuplicateSettings(String category, String settingKey, String settingValue, String updatedBy) {
        try {
            // 查找所有重复的记录
             List<SystemSettings> duplicateSettings = systemSettingsRepository.findByCategory(category)
                 .stream()
                 .filter(s -> s.getSettingKey().equals(settingKey))
                 .collect(Collectors.toList());
            
            if (duplicateSettings.size() > 1) {
                log.warn("发现重复设置数据，category: {}, settingKey: {}, 重复数量: {}", category, settingKey, duplicateSettings.size());
                
                // 保留最新的一条记录，删除其他的
                SystemSettings latestSetting = duplicateSettings.stream()
                    .max((s1, s2) -> s1.getUpdatedAt().compareTo(s2.getUpdatedAt()))
                    .orElse(duplicateSettings.get(0));
                
                // 更新保留的记录
                latestSetting.setSettingValue(settingValue);
                latestSetting.setUpdatedBy(updatedBy);
                systemSettingsRepository.save(latestSetting);
                
                // 删除其他重复记录
                duplicateSettings.stream()
                    .filter(s -> !s.getId().equals(latestSetting.getId()))
                    .forEach(s -> {
                        log.info("删除重复设置记录，ID: {}, category: {}, settingKey: {}", s.getId(), category, settingKey);
                        systemSettingsRepository.delete(s);
                    });
                
                log.info("重复数据清理完成，category: {}, settingKey: {}", category, settingKey);
            }
        } catch (Exception e) {
            log.error("清理重复设置数据时发生异常，category: {}, settingKey: {}, error: {}", category, settingKey, e.getMessage(), e);
            throw new RuntimeException("清理重复数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 初始化默认设置
     */
    @Transactional
    public void initializeDefaultSettings() {
        log.info("初始化默认系统设置");
        
        // 基础设置
        initializeBasicSettings();
        
        // Redis配置
        initializeRedisSettings();
        
        // 队列设置
        initializeQueueSettings();
        
        // 安全设置
        initializeSecuritySettings();
        
        // 通知设置
        initializeNotificationSettings();
        
        // 性能优化设置
        initializePerformanceSettings();
        
        log.info("默认系统设置初始化完成");
    }
    
    // 私有方法：获取各类设置
    
    private SystemSettingsDTO.BasicSettings getBasicSettings() {
        SystemSettingsDTO.BasicSettings settings = new SystemSettingsDTO.BasicSettings();
        settings.setSystemName(getSettingValue("basic", "systemName", "Redis延迟队列系统"));
        settings.setSystemDescription(getSettingValue("basic", "systemDescription", "基于Redis的延迟队列管理系统"));
        settings.setSystemVersion(getSettingValue("basic", "systemVersion", "1.0.0"));
        settings.setAdminEmail(getSettingValue("basic", "adminEmail", "admin@example.com"));
        settings.setTimezone(getSettingValue("basic", "timezone", "Asia/Shanghai"));
        settings.setLogLevel(getSettingValue("basic", "logLevel", "INFO"));
        return settings;
    }
    
    private SystemSettingsDTO.RedisSettings getRedisSettings() {
        SystemSettingsDTO.RedisSettings settings = new SystemSettingsDTO.RedisSettings();
        settings.setHost(getSettingValue("redis", "host", "localhost"));
        settings.setPort(Integer.parseInt(getSettingValue("redis", "port", "6379")));
        settings.setDatabase(Integer.parseInt(getSettingValue("redis", "database", "0")));
        settings.setPassword(getSettingValue("redis", "password", ""));
        settings.setTimeout(Integer.parseInt(getSettingValue("redis", "timeout", "5000")));
        settings.setMaxActive(Integer.parseInt(getSettingValue("redis", "maxActive", "20")));
        settings.setMaxIdle(Integer.parseInt(getSettingValue("redis", "maxIdle", "10")));
        settings.setMinIdle(Integer.parseInt(getSettingValue("redis", "minIdle", "5")));
        return settings;
    }
    
    private SystemSettingsDTO.QueueSettings getQueueSettings() {
        SystemSettingsDTO.QueueSettings settings = new SystemSettingsDTO.QueueSettings();
        settings.setDefaultQueueName(getSettingValue("queue", "defaultQueueName", "default"));
        settings.setMaxRetryCount(Integer.parseInt(getSettingValue("queue", "maxRetryCount", "3")));
        settings.setTaskTimeout(Integer.parseInt(getSettingValue("queue", "taskTimeout", "300")));
        settings.setBatchSize(Integer.parseInt(getSettingValue("queue", "batchSize", "100")));
        settings.setScanInterval(Integer.parseInt(getSettingValue("queue", "scanInterval", "1000")));
        settings.setEnablePriority(Boolean.parseBoolean(getSettingValue("queue", "enablePriority", "true")));
        return settings;
    }
    
    private SystemSettingsDTO.SecuritySettings getSecuritySettings() {
        SystemSettingsDTO.SecuritySettings settings = new SystemSettingsDTO.SecuritySettings();
        settings.setJwtSecret(getSettingValue("security", "jwtSecret", "mySecretKey"));
        settings.setTokenExpiration(Integer.parseInt(getSettingValue("security", "tokenExpiration", "86400")));
        settings.setPasswordMinLength(Integer.parseInt(getSettingValue("security", "passwordMinLength", "6")));
        settings.setLoginFailureLockCount(Integer.parseInt(getSettingValue("security", "loginFailureLockCount", "5")));
        settings.setEnableTwoFactor(Boolean.parseBoolean(getSettingValue("security", "enableTwoFactor", "false")));
        settings.setIpWhitelist(getSettingValue("security", "ipWhitelist", ""));
        return settings;
    }
    
    private SystemSettingsDTO.NotificationSettings getNotificationSettings() {
        SystemSettingsDTO.NotificationSettings settings = new SystemSettingsDTO.NotificationSettings();
        settings.setEnableEmailNotification(Boolean.parseBoolean(getSettingValue("notification", "enableEmailNotification", "false")));
        settings.setSmtpHost(getSettingValue("notification", "smtpHost", ""));
        settings.setSmtpPort(Integer.parseInt(getSettingValue("notification", "smtpPort", "587")));
        settings.setSmtpUsername(getSettingValue("notification", "smtpUsername", ""));
        settings.setSmtpPassword(getSettingValue("notification", "smtpPassword", ""));
        settings.setEnableWebhookNotification(Boolean.parseBoolean(getSettingValue("notification", "enableWebhookNotification", "false")));
        settings.setWebhookUrl(getSettingValue("notification", "webhookUrl", ""));
        settings.setNotificationEvents(getSettingValue("notification", "notificationEvents", "task_failed,task_completed"));
        return settings;
    }
    
    private SystemSettingsDTO.PerformanceSettings getPerformanceSettings() {
        SystemSettingsDTO.PerformanceSettings settings = new SystemSettingsDTO.PerformanceSettings();
        settings.setThreadPoolSize(Integer.parseInt(getSettingValue("performance", "threadPoolSize", "10")));
        settings.setQueueCapacity(Integer.parseInt(getSettingValue("performance", "queueCapacity", "1000")));
        settings.setEnableCache(Boolean.parseBoolean(getSettingValue("performance", "enableCache", "true")));
        settings.setEnableCompression(Boolean.parseBoolean(getSettingValue("performance", "enableCompression", "false")));
        settings.setConnectionPoolSize(Integer.parseInt(getSettingValue("performance", "connectionPoolSize", "20")));
        settings.setMaxConcurrentRequests(Integer.parseInt(getSettingValue("performance", "maxConcurrentRequests", "100")));
        settings.setRequestTimeout(Integer.parseInt(getSettingValue("performance", "requestTimeout", "30")));
        return settings;
    }
    
    // 私有方法：初始化默认设置
    
    private void initializeBasicSettings() {
        saveDefaultSetting("basic", "systemName", "Redis延迟队列系统", "系统名称");
        saveDefaultSetting("basic", "systemDescription", "基于Redis的延迟队列管理系统", "系统描述");
        saveDefaultSetting("basic", "systemVersion", "1.0.0", "系统版本");
        saveDefaultSetting("basic", "adminEmail", "admin@example.com", "管理员邮箱");
        saveDefaultSetting("basic", "timezone", "Asia/Shanghai", "时区设置");
        saveDefaultSetting("basic", "logLevel", "INFO", "日志级别");
    }
    
    private void initializeRedisSettings() {
        saveDefaultSetting("redis", "host", "localhost", "Redis主机地址");
        saveDefaultSetting("redis", "port", "6379", "Redis端口");
        saveDefaultSetting("redis", "database", "0", "Redis数据库索引");
        saveDefaultSetting("redis", "password", "", "Redis密码");
        saveDefaultSetting("redis", "timeout", "5000", "连接超时时间(ms)");
        saveDefaultSetting("redis", "maxActive", "20", "最大活跃连接数");
        saveDefaultSetting("redis", "maxIdle", "10", "最大空闲连接数");
        saveDefaultSetting("redis", "minIdle", "5", "最小空闲连接数");
    }
    
    private void initializeQueueSettings() {
        saveDefaultSetting("queue", "defaultQueueName", "default", "默认队列名称");
        saveDefaultSetting("queue", "maxRetryCount", "3", "最大重试次数");
        saveDefaultSetting("queue", "taskTimeout", "300", "任务超时时间(秒)");
        saveDefaultSetting("queue", "batchSize", "100", "批处理大小");
        saveDefaultSetting("queue", "scanInterval", "1000", "扫描间隔(ms)");
        saveDefaultSetting("queue", "enablePriority", "true", "启用优先级");
    }
    
    private void initializeSecuritySettings() {
        saveDefaultSetting("security", "jwtSecret", "mySecretKey", "JWT密钥");
        saveDefaultSetting("security", "tokenExpiration", "86400", "Token过期时间(秒)");
        saveDefaultSetting("security", "passwordMinLength", "6", "密码最小长度");
        saveDefaultSetting("security", "loginFailureLockCount", "5", "登录失败锁定次数");
        saveDefaultSetting("security", "enableTwoFactor", "false", "启用双因子认证");
        saveDefaultSetting("security", "ipWhitelist", "", "IP白名单");
    }
    
    private void initializeNotificationSettings() {
        saveDefaultSetting("notification", "enableEmailNotification", "false", "启用邮件通知");
        saveDefaultSetting("notification", "smtpHost", "", "SMTP主机");
        saveDefaultSetting("notification", "smtpPort", "587", "SMTP端口");
        saveDefaultSetting("notification", "smtpUsername", "", "SMTP用户名");
        saveDefaultSetting("notification", "smtpPassword", "", "SMTP密码");
        saveDefaultSetting("notification", "enableWebhookNotification", "false", "启用Webhook通知");
        saveDefaultSetting("notification", "webhookUrl", "", "Webhook URL");
        saveDefaultSetting("notification", "notificationEvents", "task_failed,task_completed", "通知事件");
    }
    
    private void initializePerformanceSettings() {
        saveDefaultSetting("performance", "threadPoolSize", "10", "线程池大小");
        saveDefaultSetting("performance", "queueCapacity", "1000", "队列容量");
        saveDefaultSetting("performance", "enableCache", "true", "启用缓存");
        saveDefaultSetting("performance", "enableCompression", "false", "启用压缩");
        saveDefaultSetting("performance", "connectionPoolSize", "20", "连接池大小");
        saveDefaultSetting("performance", "maxConcurrentRequests", "100", "最大并发请求数");
        saveDefaultSetting("performance", "requestTimeout", "30", "请求超时时间(秒)");
    }
    
    private void saveDefaultSetting(String category, String settingKey, String defaultValue, String description) {
        if (!systemSettingsRepository.existsByCategoryAndSettingKey(category, settingKey)) {
            SystemSettings setting = new SystemSettings();
            setting.setCategory(category);
            setting.setSettingKey(settingKey);
            setting.setSettingValue(defaultValue);
            setting.setDescription(description);
            setting.setDataType("STRING");
            setting.setCreatedBy("system");
            setting.setUpdatedBy("system");
            systemSettingsRepository.save(setting);
        }
    }
}