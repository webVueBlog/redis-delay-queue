package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基础健康检查接口
     */
    @GetMapping("/")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("application", "Redis Delay Queue Application");
        result.put("version", "1.0.0");
        return result;
    }

    /**
     * 详细健康检查接口
     */
    @GetMapping("/detailed")
    public Map<String, Object> detailedHealth() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("application", "Redis Delay Queue Application");
        result.put("version", "1.0.0");
        
        // 检查数据库连接
        Map<String, Object> database = checkDatabase();
        result.put("database", database);
        
        // 检查Redis连接
        Map<String, Object> redis = checkRedis();
        result.put("redis", redis);
        
        // 整体状态判断
        boolean isHealthy = "UP".equals(database.get("status")) && "UP".equals(redis.get("status"));
        result.put("status", isHealthy ? "UP" : "DOWN");
        
        return result;
    }

    /**
     * 数据库连接检查
     */
    @GetMapping("/database")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 执行简单查询测试数据库连接
            String dbTime = jdbcTemplate.queryForObject("SELECT NOW()", String.class);
            result.put("status", "UP");
            result.put("message", "Database connection is healthy");
            result.put("serverTime", dbTime);
            result.put("responseTime", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("message", "Database connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        return result;
    }

    /**
     * Redis连接检查
     */
    @GetMapping("/redis")
    public Map<String, Object> checkRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试Redis连接
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "test";
            
            // 写入测试
            redisTemplate.opsForValue().set(testKey, testValue);
            
            // 读取测试
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            // 删除测试键
            redisTemplate.delete(testKey);
            
            if (testValue.equals(retrievedValue)) {
                result.put("status", "UP");
                result.put("message", "Redis connection is healthy");
                result.put("responseTime", System.currentTimeMillis());
            } else {
                result.put("status", "DOWN");
                result.put("message", "Redis read/write test failed");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("message", "Redis connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        return result;
    }

    /**
     * 应用信息接口
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("application", "Redis Delay Queue Application");
        result.put("version", "1.0.0");
        result.put("description", "A Spring Boot application with Redis delay queue functionality");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("totalMemory", runtime.totalMemory());
        jvm.put("freeMemory", runtime.freeMemory());
        jvm.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvm.put("maxMemory", runtime.maxMemory());
        jvm.put("availableProcessors", runtime.availableProcessors());
        result.put("jvm", jvm);
        
        return result;
    }

    /**
     * 就绪检查接口（用于Kubernetes readiness probe）
     */
    @GetMapping("/ready")
    public Map<String, Object> ready() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查关键依赖是否就绪
        boolean databaseReady = "UP".equals(checkDatabase().get("status"));
        boolean redisReady = "UP".equals(checkRedis().get("status"));
        
        boolean isReady = databaseReady && redisReady;
        
        result.put("status", isReady ? "READY" : "NOT_READY");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("checks", Map.of(
            "database", databaseReady ? "READY" : "NOT_READY",
            "redis", redisReady ? "READY" : "NOT_READY"
        ));
        
        return result;
    }

    /**
     * 存活检查接口（用于Kubernetes liveness probe）
     */
    @GetMapping("/live")
    public Map<String, Object> live() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ALIVE");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("uptime", System.currentTimeMillis());
        return result;
    }
}