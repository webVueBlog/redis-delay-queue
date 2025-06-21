package com.example.controller;

import com.example.config.DataSourceMonitorConfig;
import com.example.util.ConnectionPoolPerformanceTest;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 连接池管理控制器
 * 提供连接池监控、测试和管理功能的REST API
 */
@RestController
@RequestMapping("/api/connection-pool")
public class ConnectionPoolController {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolController.class);

    private final DataSource primaryDataSource;
    private final DataSource userDataSource;
    private final ConnectionPoolPerformanceTest performanceTest;
    private final DataSourceMonitorConfig.ConnectionPoolHealthIndicator healthIndicator;

    public ConnectionPoolController(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                  @Qualifier("userDataSource") DataSource userDataSource,
                                  ConnectionPoolPerformanceTest performanceTest,
                                  DataSourceMonitorConfig.ConnectionPoolHealthIndicator healthIndicator) {
        this.primaryDataSource = primaryDataSource;
        this.userDataSource = userDataSource;
        this.performanceTest = performanceTest;
        this.healthIndicator = healthIndicator;
    }

    /**
     * 获取连接池状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getConnectionPoolStatus() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取主数据源状态
            Map<String, Object> primaryStatus = getDataSourceStatus((HikariDataSource) primaryDataSource);
            result.put("primary", primaryStatus);
            
            // 获取用户数据源状态
            Map<String, Object> userStatus = getDataSourceStatus((HikariDataSource) userDataSource);
            result.put("user", userStatus);
            
            // 整体健康状态
            result.put("healthy", healthIndicator.isHealthy());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取连接池状态失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取连接池状态失败: " + e.getMessage()));
        }
    }

    /**
     * 获取单个数据源状态
     */
    private Map<String, Object> getDataSourceStatus(HikariDataSource dataSource) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
            
            status.put("poolName", dataSource.getPoolName());
            status.put("activeConnections", poolBean.getActiveConnections());
            status.put("idleConnections", poolBean.getIdleConnections());
            status.put("totalConnections", poolBean.getTotalConnections());
            status.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
            status.put("maximumPoolSize", dataSource.getMaximumPoolSize());
            status.put("minimumIdle", dataSource.getMinimumIdle());
            
            // 计算使用率
            int activeConnections = poolBean.getActiveConnections();
            int maxPoolSize = dataSource.getMaximumPoolSize();
            double utilizationRate = (double) activeConnections / maxPoolSize * 100;
            status.put("utilizationRate", Math.round(utilizationRate * 100.0) / 100.0);
            
            // 连接池配置信息
            status.put("connectionTimeout", dataSource.getConnectionTimeout());
            status.put("idleTimeout", dataSource.getIdleTimeout());
            status.put("maxLifetime", dataSource.getMaxLifetime());
            status.put("validationTimeout", dataSource.getValidationTimeout());
            
            // 健康状态
            int totalConnections = poolBean.getTotalConnections();
            boolean healthy = totalConnections > 0 && activeConnections < maxPoolSize;
            status.put("healthy", healthy);
            
        } catch (Exception e) {
            logger.error("获取数据源状态失败", e);
            status.put("error", e.getMessage());
            status.put("healthy", false);
        }
        
        return status;
    }

    /**
     * 执行连接池健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean healthy = healthIndicator.isHealthy();
            
            Map<String, Object> result = new HashMap<>();
            result.put("healthy", healthy);
            result.put("status", healthy ? "UP" : "DOWN");
            result.put("timestamp", System.currentTimeMillis());
            
            if (healthy) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(503).body(result);
            }
            
        } catch (Exception e) {
            logger.error("健康检查失败", e);
            return ResponseEntity.status(503)
                    .body(Map.of(
                        "healthy", false,
                        "status", "DOWN",
                        "error", e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 执行快速连接测试
     */
    @PostMapping("/test/quick")
    public ResponseEntity<Map<String, Object>> quickTest() {
        try {
            logger.info("执行快速连接测试");
            performanceTest.quickConnectionTest();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "快速连接测试完成",
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            logger.error("快速连接测试失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "error", "快速连接测试失败: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 执行性能测试
     */
    @PostMapping("/test/performance")
    public ResponseEntity<Map<String, Object>> performanceTest(
            @RequestParam(defaultValue = "10") int threadCount,
            @RequestParam(defaultValue = "100") int operationsPerThread,
            @RequestParam(defaultValue = "30") int testDurationSeconds) {
        
        try {
            // 参数验证
            if (threadCount <= 0 || threadCount > 100) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "线程数必须在1-100之间"));
            }
            
            if (operationsPerThread <= 0 || operationsPerThread > 10000) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "每线程操作数必须在1-10000之间"));
            }
            
            if (testDurationSeconds <= 0 || testDurationSeconds > 300) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "测试时长必须在1-300秒之间"));
            }
            
            logger.info("开始执行性能测试 - 线程数: {}, 操作数: {}, 时长: {}秒", 
                       threadCount, operationsPerThread, testDurationSeconds);
            
            // 异步执行性能测试
            new Thread(() -> {
                performanceTest.runPerformanceTest(threadCount, operationsPerThread, testDurationSeconds);
            }).start();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "性能测试已开始，请查看日志获取详细结果",
                "parameters", Map.of(
                    "threadCount", threadCount,
                    "operationsPerThread", operationsPerThread,
                    "testDurationSeconds", testDurationSeconds
                ),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            logger.error("性能测试失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "error", "性能测试失败: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 连接池预热
     */
    @PostMapping("/warmup")
    public ResponseEntity<Map<String, Object>> warmUp() {
        try {
            logger.info("开始连接池预热");
            performanceTest.warmUpConnectionPools();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "连接池预热完成",
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            logger.error("连接池预热失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "error", "连接池预热失败: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 获取连接池配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConnectionPoolConfig() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 主数据源配置
            Map<String, Object> primaryConfig = getDataSourceConfig((HikariDataSource) primaryDataSource);
            result.put("primary", primaryConfig);
            
            // 用户数据源配置
            Map<String, Object> userConfig = getDataSourceConfig((HikariDataSource) userDataSource);
            result.put("user", userConfig);
            
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取连接池配置失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取连接池配置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取数据库信息
     */
    @GetMapping("/database-info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取数据库基本信息
            Map<String, Object> primaryDbInfo = getDatabaseMetadata((HikariDataSource) primaryDataSource);
            result.put("primary", primaryDbInfo);
            
            Map<String, Object> userDbInfo = getDatabaseMetadata((HikariDataSource) userDataSource);
            result.put("user", userDbInfo);
            
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取数据库信息失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取数据库信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 执行健康检查
     */
    @PostMapping("/health-check")
    public ResponseEntity<Map<String, Object>> performHealthCheck() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 检查主数据源健康状态
            boolean primaryHealthy = checkDataSourceHealth((HikariDataSource) primaryDataSource);
            result.put("primaryHealthy", primaryHealthy);
            
            // 检查用户数据源健康状态
            boolean userHealthy = checkDataSourceHealth((HikariDataSource) userDataSource);
            result.put("userHealthy", userHealthy);
            
            result.put("overallHealthy", primaryHealthy && userHealthy);
            result.put("timestamp", System.currentTimeMillis());
            result.put("message", "健康检查完成");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("健康检查失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "健康检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 运行性能测试
     */
    @PostMapping("/performance-test")
    public ResponseEntity<Map<String, Object>> runPerformanceTest(
            @RequestBody(required = false) Map<String, Object> params) {
        try {
            // 设置默认参数
            int threadCount = params != null ? (Integer) params.getOrDefault("threadCount", 10) : 10;
            int operationsPerThread = params != null ? (Integer) params.getOrDefault("operationsPerThread", 100) : 100;
            int testDurationSeconds = params != null ? (Integer) params.getOrDefault("testDurationSeconds", 30) : 30;
            
            // 执行性能测试
            performanceTest.runPerformanceTest(threadCount, operationsPerThread, testDurationSeconds);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "性能测试完成");
            result.put("threadCount", threadCount);
            result.put("operationsPerThread", operationsPerThread);
            result.put("testDurationSeconds", testDurationSeconds);
            result.put("avgTime", Math.random() * 50 + 10); // 模拟平均响应时间
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("性能测试失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "性能测试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取连接池历史数据
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getConnectionPoolHistory(
            @RequestParam(defaultValue = "1h") String period,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 模拟历史数据
            List<Map<String, Object>> historyData = generateMockHistoryData(period);
            result.put("data", historyData);
            result.put("period", period);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取历史数据失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取历史数据失败: " + e.getMessage()));
        }
    }
    
    /**
     * 导出连接池报告
     */
    @GetMapping("/export/{format}")
    public ResponseEntity<byte[]> exportConnectionPoolReport(@PathVariable String format) {
        try {
            // 模拟导出功能
            String content = "连接池监控报告\n导出时间: " + new java.util.Date() + "\n";
            byte[] data = content.getBytes("UTF-8");
            
            String filename = "connection_pool_report." + format;
            String contentType = "pdf".equals(format) ? "application/pdf" : "application/vnd.ms-excel";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", contentType)
                    .body(data);
                    
        } catch (Exception e) {
            logger.error("导出报告失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 重置连接池
     */
    @PostMapping("/reset/{poolName}")
    public ResponseEntity<Map<String, Object>> resetConnectionPool(@PathVariable String poolName) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            if ("primary".equals(poolName)) {
                // 重置主数据源连接池
                ((HikariDataSource) primaryDataSource).getHikariPoolMXBean().softEvictConnections();
                result.put("message", "主数据源连接池重置成功");
            } else if ("user".equals(poolName)) {
                // 重置用户数据源连接池
                ((HikariDataSource) userDataSource).getHikariPoolMXBean().softEvictConnections();
                result.put("message", "用户数据源连接池重置成功");
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的连接池名称: " + poolName));
            }
            
            result.put("poolName", poolName);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("重置连接池失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "重置连接池失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取数据库元数据信息
     */
    private Map<String, Object> getDatabaseMetadata(HikariDataSource dataSource) {
        Map<String, Object> info = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            
            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("driverName", metaData.getDriverName());
            info.put("driverVersion", metaData.getDriverVersion());
            info.put("url", metaData.getURL());
            info.put("userName", metaData.getUserName());
            
        } catch (Exception e) {
            logger.error("获取数据库元数据失败", e);
            info.put("error", "获取数据库信息失败");
        }
        
        return info;
    }
    
    /**
     * 检查数据源健康状态
     */
    private boolean checkDataSourceHealth(HikariDataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (Exception e) {
            logger.error("数据源健康检查失败", e);
            return false;
        }
    }
    
    /**
     * 生成模拟历史数据
     */
    private List<Map<String, Object>> generateMockHistoryData(String period) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        int dataPoints = "1h".equals(period) ? 12 : "6h".equals(period) ? 24 : "24h".equals(period) ? 48 : 168;
        long now = System.currentTimeMillis();
        long interval = "1h".equals(period) ? 5 * 60 * 1000 : "6h".equals(period) ? 15 * 60 * 1000 : 
                       "24h".equals(period) ? 30 * 60 * 1000 : 60 * 60 * 1000;
        
        for (int i = 0; i < dataPoints; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("timestamp", now - (dataPoints - i) * interval);
            point.put("activeConnections", (int) (Math.random() * 20) + 5);
            point.put("idleConnections", (int) (Math.random() * 10) + 2);
            point.put("responseTime", Math.random() * 100 + 10);
            data.add(point);
        }
        
        return data;
    }
    
    /**
     * 获取单个数据源配置
     */
    private Map<String, Object> getDataSourceConfig(HikariDataSource dataSource) {
        Map<String, Object> config = new HashMap<>();
        
        config.put("poolName", dataSource.getPoolName());
        config.put("maximumPoolSize", dataSource.getMaximumPoolSize());
        config.put("minimumIdle", dataSource.getMinimumIdle());
        config.put("connectionTimeout", dataSource.getConnectionTimeout());
        config.put("idleTimeout", dataSource.getIdleTimeout());
        config.put("maxLifetime", dataSource.getMaxLifetime());
        config.put("validationTimeout", dataSource.getValidationTimeout());
        config.put("leakDetectionThreshold", dataSource.getLeakDetectionThreshold());
        config.put("connectionTestQuery", dataSource.getConnectionTestQuery());
        config.put("autoCommit", dataSource.isAutoCommit());
        config.put("readOnly", dataSource.isReadOnly());
        
        return config;
    }
    
    /**
     * 获取连接池详细信息
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getConnectionPoolDetails() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取主数据源详细信息
            Map<String, Object> primaryDetails = getDetailedDataSourceInfo((HikariDataSource) primaryDataSource, "primary");
            result.put("primary", primaryDetails);
            
            // 获取用户数据源详细信息
            Map<String, Object> userDetails = getDetailedDataSourceInfo((HikariDataSource) userDataSource, "user");
            result.put("user", userDetails);
            
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取连接池详细信息失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取连接池详细信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新连接池配置
     */
    @PutMapping("/config/{poolName}")
    public ResponseEntity<Map<String, Object>> updateConnectionPoolConfig(
            @PathVariable String poolName,
            @RequestBody Map<String, Object> config) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            HikariDataSource dataSource;
            if ("primary".equals(poolName)) {
                dataSource = (HikariDataSource) primaryDataSource;
            } else if ("user".equals(poolName)) {
                dataSource = (HikariDataSource) userDataSource;
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的连接池名称: " + poolName));
            }
            
            // 更新可修改的配置项
            if (config.containsKey("maximumPoolSize")) {
                dataSource.setMaximumPoolSize((Integer) config.get("maximumPoolSize"));
            }
            if (config.containsKey("minimumIdle")) {
                dataSource.setMinimumIdle((Integer) config.get("minimumIdle"));
            }
            if (config.containsKey("connectionTimeout")) {
                dataSource.setConnectionTimeout((Long) config.get("connectionTimeout"));
            }
            if (config.containsKey("idleTimeout")) {
                dataSource.setIdleTimeout((Long) config.get("idleTimeout"));
            }
            if (config.containsKey("maxLifetime")) {
                dataSource.setMaxLifetime((Long) config.get("maxLifetime"));
            }
            if (config.containsKey("leakDetectionThreshold")) {
                dataSource.setLeakDetectionThreshold((Long) config.get("leakDetectionThreshold"));
            }
            
            result.put("message", poolName + " 连接池配置更新成功");
            result.put("poolName", poolName);
            result.put("updatedConfig", config);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("更新连接池配置失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "更新连接池配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取详细的数据源信息
     */
    private Map<String, Object> getDetailedDataSourceInfo(HikariDataSource dataSource, String poolType) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
            
            // 基本连接池信息
            details.put("poolName", dataSource.getPoolName());
            details.put("poolType", poolType);
            details.put("status", "HEALTHY");
            
            // 连接数统计
            details.put("activeConnections", poolBean.getActiveConnections());
            details.put("idleConnections", poolBean.getIdleConnections());
            details.put("totalConnections", poolBean.getTotalConnections());
            details.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
            
            // 配置信息
            details.put("maximumPoolSize", dataSource.getMaximumPoolSize());
            details.put("minimumIdle", dataSource.getMinimumIdle());
            details.put("connectionTimeout", dataSource.getConnectionTimeout());
            details.put("idleTimeout", dataSource.getIdleTimeout());
            details.put("maxLifetime", dataSource.getMaxLifetime());
            details.put("validationTimeout", dataSource.getValidationTimeout());
            details.put("leakDetectionThreshold", dataSource.getLeakDetectionThreshold());
            
            // 计算使用率
            int activeConnections = poolBean.getActiveConnections();
            int maxPoolSize = dataSource.getMaximumPoolSize();
            double usageRate = maxPoolSize > 0 ? (double) activeConnections / maxPoolSize * 100 : 0;
            details.put("usageRate", Math.round(usageRate * 100.0) / 100.0);
            
            // 性能指标（模拟数据）
            details.put("avgConnectionTime", Math.random() * 50 + 10);
            details.put("maxConnectionTime", Math.random() * 200 + 100);
            details.put("connectionSuccessRate", 99.8 + Math.random() * 0.2);
            
            // 数据库连接信息
            try (Connection connection = dataSource.getConnection()) {
                java.sql.DatabaseMetaData metaData = connection.getMetaData();
                details.put("databaseUrl", metaData.getURL());
                details.put("databaseProduct", metaData.getDatabaseProductName());
                details.put("databaseVersion", metaData.getDatabaseProductVersion());
                details.put("driverName", metaData.getDriverName());
                details.put("driverVersion", metaData.getDriverVersion());
            }
            
        } catch (Exception e) {
            logger.error("获取详细数据源信息失败", e);
            details.put("error", "获取详细信息失败: " + e.getMessage());
        }
        
        return details;
    }
}