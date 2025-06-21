package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringDataDTO {
    
    @JsonProperty("systemInfo")
    private SystemInfo systemInfo;
    
    @JsonProperty("redisInfo")
    private RedisInfo redisInfo;
    
    @JsonProperty("queueStats")
    private QueueStats queueStats;
    
    @JsonProperty("performanceMetrics")
    private PerformanceMetrics performanceMetrics;
    
    @JsonProperty("apiStats")
    private ApiStats apiStats;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInfo {
        private Double cpuUsage;
        private Double memoryUsage;
        private Long totalMemory;
        private Long usedMemory;
        private Long maxMemory;
        private Integer availableProcessors;
        private Double systemLoadAverage;
        private String jvmVersion;
        private String osName;
        private String osVersion;
        private Long uptime;
        private Long startTime;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisInfo {
        private String status;
        private String redisVersion;
        private Integer connectedClients;
        private Long usedMemory;
        private Long maxMemory;
        private Double memoryUsagePercentage;
        private Long totalKeys;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueStats {
        private Long totalTasks;
        private Long pendingTasks;
        private Long processingTasks;
        private Long completedTasks;
        private Long failedTasks;
        private Double processingRate;
        private Integer avgProcessingTime;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private Double avgResponseTime;
        private Long qps;
        private String qpsTrend;
        private Double qpsChange;
        private Double errorRate;
        private Integer activeConnections;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiStats {
        private Map<String, EndpointStats> endpoints;
        private Long totalRequests;
        private Long totalErrors;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointStats {
        private Long requests;
        private Double avgResponseTime;
        private Double errorRate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryData {
        private List<Object> cpu;
        private List<Object> memory;
        private List<Object> queueTasks;
        private List<Object> apiRequests;
        private List<String> timestamps;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthSummary {
        private String status;
        private Double cpuUsage;
        private Double memoryUsage;
        private String redisStatus;
        private Long totalTasks;
        private Long failedTasks;
    }
}