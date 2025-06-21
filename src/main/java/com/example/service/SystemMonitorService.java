package com.example.service;

import com.example.queue.RedisDelayQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemMonitorService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisDelayQueue redisDelayQueue;
    
    // 性能指标缓存
    private final Map<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> responseTimeCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    // 历史数据存储键
    private static final String HISTORY_KEY_PREFIX = "monitor:history:";
    private static final String CPU_HISTORY_KEY = HISTORY_KEY_PREFIX + "cpu";
    private static final String MEMORY_HISTORY_KEY = HISTORY_KEY_PREFIX + "memory";
    private static final String QUEUE_HISTORY_KEY = HISTORY_KEY_PREFIX + "queue";
    private static final String API_HISTORY_KEY = HISTORY_KEY_PREFIX + "api";
    
    /**
     * 获取系统信息
     */
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        try {
            com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            Runtime runtime = Runtime.getRuntime();
            
            // CPU信息
            double cpuUsage = osBean.getProcessCpuLoad() * 100;
            if (cpuUsage < 0) {
                cpuUsage = 0; // 某些系统可能返回负值
            }
            systemInfo.put("cpuUsage", Math.round(cpuUsage * 100.0) / 100.0);
            systemInfo.put("availableProcessors", osBean.getAvailableProcessors());
            systemInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
            
            // 内存信息
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            systemInfo.put("totalMemory", totalMemory);
            systemInfo.put("freeMemory", freeMemory);
            systemInfo.put("usedMemory", usedMemory);
            systemInfo.put("maxMemory", maxMemory);
            systemInfo.put("memoryUsage", Math.round((double) usedMemory / maxMemory * 100 * 100.0) / 100.0);
            
            // JVM信息
            systemInfo.put("jvmVersion", System.getProperty("java.version"));
            systemInfo.put("jvmVendor", System.getProperty("java.vendor"));
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osVersion", System.getProperty("os.version"));
            
            // 应用运行时间
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            systemInfo.put("uptime", uptime);
            systemInfo.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
            
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            systemInfo.put("error", e.getMessage());
        }
        
        return systemInfo;
    }
    
    /**
     * 获取Redis信息
     */
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> redisInfo = new HashMap<>();
        
        try {
            // 获取Redis连接信息
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info();
            
            redisInfo.put("status", "UP");
            redisInfo.put("redisVersion", info.getProperty("redis_version", "Unknown"));
            redisInfo.put("connectedClients", Integer.parseInt(info.getProperty("connected_clients", "0")));
            redisInfo.put("usedMemory", Long.parseLong(info.getProperty("used_memory", "0")));
            redisInfo.put("maxMemory", Long.parseLong(info.getProperty("maxmemory", "0")));
            
            // 计算内存使用率
            long usedMemory = Long.parseLong(info.getProperty("used_memory", "0"));
            long maxMemory = Long.parseLong(info.getProperty("maxmemory", "0"));
            if (maxMemory > 0) {
                double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;
                redisInfo.put("memoryUsagePercentage", Math.round(memoryUsagePercentage * 100.0) / 100.0);
            } else {
                redisInfo.put("memoryUsagePercentage", 0.0);
            }
            
            // 获取键数量
            Long totalKeys = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Long>) connection1 -> {
                try {
                    Properties dbInfo = connection1.info("keyspace");
                    long keys = 0;
                    for (String key : dbInfo.stringPropertyNames()) {
                        if (key.startsWith("db")) {
                            String value = dbInfo.getProperty(key);
                            String[] parts = value.split(",");
                            if (parts.length > 0) {
                                String keysPart = parts[0];
                                if (keysPart.startsWith("keys=")) {
                                    keys += Long.parseLong(keysPart.substring(5));
                                }
                            }
                        }
                    }
                    return keys;
                } catch (Exception e) {
                    return 0L;
                }
            });
            redisInfo.put("totalKeys", totalKeys != null ? totalKeys : 0);
            
            connection.close();
            
        } catch (Exception e) {
            log.error("获取Redis信息失败", e);
            redisInfo.put("status", "DOWN");
            redisInfo.put("error", e.getMessage());
            redisInfo.put("connectedClients", 0);
            redisInfo.put("usedMemory", 0L);
            redisInfo.put("totalKeys", 0L);
            redisInfo.put("memoryUsagePercentage", 0.0);
        }
        
        return redisInfo;
    }
    
    /**
     * 获取队列统计信息
     */
    public Map<String, Object> getQueueStats() {
        Map<String, Object> queueStats = new HashMap<>();
        
        try {
            Map<String, Long> taskStats = redisDelayQueue.getTaskStats();
            
            queueStats.put("totalTasks", taskStats.getOrDefault("total", 0L));
            queueStats.put("pendingTasks", taskStats.getOrDefault("pending", 0L));
            queueStats.put("processingTasks", taskStats.getOrDefault("processing", 0L));
            queueStats.put("completedTasks", taskStats.getOrDefault("completed", 0L));
            queueStats.put("failedTasks", taskStats.getOrDefault("failed", 0L));
            
            // 计算处理率
            long total = taskStats.getOrDefault("total", 0L);
            long completed = taskStats.getOrDefault("completed", 0L);
            double processingRate = total > 0 ? (double) completed / total * 100 : 0;
            queueStats.put("processingRate", Math.round(processingRate * 100.0) / 100.0);
            
            // 模拟平均处理时间（实际项目中应该从真实数据计算）
            queueStats.put("avgProcessingTime", 150 + new Random().nextInt(100));
            
        } catch (Exception e) {
            log.error("获取队列统计信息失败", e);
            queueStats.put("totalTasks", 0L);
            queueStats.put("pendingTasks", 0L);
            queueStats.put("processingTasks", 0L);
            queueStats.put("completedTasks", 0L);
            queueStats.put("failedTasks", 0L);
            queueStats.put("processingRate", 0.0);
            queueStats.put("avgProcessingTime", 0);
        }
        
        return queueStats;
    }
    
    /**
     * 获取性能指标
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalReq = totalRequests.get();
        long totalResp = totalResponseTime.get();
        long totalErr = totalErrors.get();
        
        // 平均响应时间
        double avgResponseTime = totalReq > 0 ? (double) totalResp / totalReq : 0;
        metrics.put("avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
        
        // QPS（简化计算，实际应该基于时间窗口）
        metrics.put("qps", totalReq);
        metrics.put("qpsTrend", "up"); // 模拟趋势
        metrics.put("qpsChange", 5.2); // 模拟变化百分比
        
        // 错误率
        double errorRate = totalReq > 0 ? (double) totalErr / totalReq * 100 : 0;
        metrics.put("errorRate", Math.round(errorRate * 100.0) / 100.0);
        
        // 活跃连接数（模拟）
        metrics.put("activeConnections", 25 + new Random().nextInt(20));
        
        return metrics;
    }
    
    /**
     * 记录API请求
     */
    public void recordApiRequest(String endpoint, long responseTime, boolean isError) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
        
        if (isError) {
            totalErrors.incrementAndGet();
            errorCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        }
        
        requestCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        responseTimeCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).addAndGet(responseTime);
    }
    
    /**
     * 获取API统计信息
     */
    public Map<String, Object> getApiStats() {
        Map<String, Object> apiStats = new HashMap<>();
        
        Map<String, Object> endpointStats = new HashMap<>();
        for (Map.Entry<String, AtomicLong> entry : requestCounters.entrySet()) {
            String endpoint = entry.getKey();
            long requests = entry.getValue().get();
            long totalTime = responseTimeCounters.getOrDefault(endpoint, new AtomicLong(0)).get();
            long errors = errorCounters.getOrDefault(endpoint, new AtomicLong(0)).get();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("requests", requests);
            stats.put("avgResponseTime", requests > 0 ? (double) totalTime / requests : 0);
            stats.put("errorRate", requests > 0 ? (double) errors / requests * 100 : 0);
            
            endpointStats.put(endpoint, stats);
        }
        
        apiStats.put("endpoints", endpointStats);
        apiStats.put("totalRequests", totalRequests.get());
        apiStats.put("totalErrors", totalErrors.get());
        
        return apiStats;
    }
    
    /**
     * 保存历史数据
     */
    public void saveHistoryData() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            // 保存CPU和内存历史数据
            Map<String, Object> systemInfo = getSystemInfo();
            Map<String, Object> cpuData = new HashMap<>();
            cpuData.put("timestamp", timestamp);
            cpuData.put("value", systemInfo.get("cpuUsage"));
            redisTemplate.opsForList().leftPush(CPU_HISTORY_KEY, cpuData);
            redisTemplate.opsForList().trim(CPU_HISTORY_KEY, 0, 99); // 保留最近100条
            
            Map<String, Object> memoryData = new HashMap<>();
            memoryData.put("timestamp", timestamp);
            memoryData.put("value", systemInfo.get("memoryUsage"));
            redisTemplate.opsForList().leftPush(MEMORY_HISTORY_KEY, memoryData);
            redisTemplate.opsForList().trim(MEMORY_HISTORY_KEY, 0, 99);
            
            // 保存队列历史数据
            Map<String, Object> queueStats = getQueueStats();
            redisTemplate.opsForList().leftPush(QUEUE_HISTORY_KEY, queueStats);
            redisTemplate.opsForList().trim(QUEUE_HISTORY_KEY, 0, 99);
            
            // 保存API历史数据
            Map<String, Object> apiStats = getApiStats();
            redisTemplate.opsForList().leftPush(API_HISTORY_KEY, apiStats);
            redisTemplate.opsForList().trim(API_HISTORY_KEY, 0, 99);
            
        } catch (Exception e) {
            log.error("保存历史数据失败", e);
        }
    }
    
    /**
     * 获取历史数据
     */
    public Map<String, Object> getHistoryData(String timeRange) {
        Map<String, Object> historyData = new HashMap<>();
        
        try {
            // 根据时间范围确定数据量
            long size;
            switch (timeRange) {
                case "1h":
                    size = 12; // 5分钟间隔，1小时12个点
                    break;
                case "6h":
                    size = 36; // 10分钟间隔，6小时36个点
                    break;
                case "24h":
                    size = 48; // 30分钟间隔，24小时48个点
                    break;
                default:
                    size = 12;
                    break;
            }
            
            List<Object> cpuHistory = redisTemplate.opsForList().range(CPU_HISTORY_KEY, 0, size - 1);
            List<Object> memoryHistory = redisTemplate.opsForList().range(MEMORY_HISTORY_KEY, 0, size - 1);
            List<Object> queueHistory = redisTemplate.opsForList().range(QUEUE_HISTORY_KEY, 0, size - 1);
            List<Object> apiHistory = redisTemplate.opsForList().range(API_HISTORY_KEY, 0, size - 1);
            
            historyData.put("cpu", cpuHistory != null ? cpuHistory : new ArrayList<>());
            historyData.put("memory", memoryHistory != null ? memoryHistory : new ArrayList<>());
            historyData.put("queueTasks", queueHistory != null ? queueHistory : new ArrayList<>());
            historyData.put("apiRequests", apiHistory != null ? apiHistory : new ArrayList<>());
            
            // 生成时间戳数组
            List<String> timestamps = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            for (int i = (int) size - 1; i >= 0; i--) {
                LocalDateTime time;
                switch (timeRange) {
                    case "1h":
                        time = now.minusMinutes(i * 5L);
                        break;
                    case "6h":
                        time = now.minusMinutes(i * 10L);
                        break;
                    case "24h":
                        time = now.minusMinutes(i * 30L);
                        break;
                    default:
                        time = now.minusMinutes(i * 5L);
                        break;
                }
                timestamps.add(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            historyData.put("timestamps", timestamps);
            
        } catch (Exception e) {
            log.error("获取历史数据失败", e);
            historyData.put("cpu", new ArrayList<>());
            historyData.put("memory", new ArrayList<>());
            historyData.put("queueTasks", new ArrayList<>());
            historyData.put("apiRequests", new ArrayList<>());
            historyData.put("timestamps", new ArrayList<>());
        }
        
        return historyData;
    }
    
    /**
     * 获取完整的监控数据
     */
    public Map<String, Object> getMonitoringData() {
        Map<String, Object> data = new HashMap<>();
        
        data.put("systemInfo", getSystemInfo());
        data.put("redisInfo", getRedisInfo());
        data.put("queueStats", getQueueStats());
        data.put("performanceMetrics", getPerformanceMetrics());
        data.put("apiStats", getApiStats());
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return data;
    }
    
    /**
     * 获取系统日志
     */
    public Map<String, Object> getLogs(int page, int size, String level, String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 模拟日志数据，实际项目中可以从日志文件或日志系统中获取
            List<Map<String, Object>> logs = new ArrayList<>();
            
            // 生成一些示例日志数据
            String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
            String[] messages = {
                "系统启动完成",
                "用户登录成功",
                "Redis连接正常",
                "队列任务处理完成",
                "API请求处理",
                "数据库连接检查",
                "内存使用率检查",
                "CPU使用率监控"
            };
            
            LocalDateTime now = LocalDateTime.now();
            int totalLogs = 1000; // 模拟总日志数
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalLogs);
            
            for (int i = startIndex; i < endIndex; i++) {
                Map<String, Object> logEntry = new HashMap<>();
                String logLevel = levels[i % levels.length];
                String message = messages[i % messages.length];
                
                // 如果指定了级别过滤
                if (level != null && !level.isEmpty() && !logLevel.equals(level)) {
                    continue;
                }
                
                // 如果指定了关键词过滤
                if (keyword != null && !keyword.isEmpty() && !message.contains(keyword)) {
                    continue;
                }
                
                logEntry.put("id", i + 1);
                logEntry.put("timestamp", now.minusMinutes(totalLogs - i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                logEntry.put("level", logLevel);
                logEntry.put("message", message + " - " + (i + 1));
                logEntry.put("source", "SystemMonitor");
                
                logs.add(logEntry);
            }
            
            result.put("logs", logs);
            result.put("total", totalLogs);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) totalLogs / size));
            
        } catch (Exception e) {
            log.error("获取系统日志失败", e);
            throw new RuntimeException("获取系统日志失败: " + e.getMessage());
        }
        
        return result;
    }
}