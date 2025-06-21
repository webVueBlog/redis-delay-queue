package com.example.controller;

import com.example.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Slf4j
public class SystemMonitorController {
    
    private final SystemMonitorService systemMonitorService;
    
    /**
     * 获取完整的监控数据
     */
    @GetMapping("/data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getMonitoringData() {
        try {
            Map<String, Object> data = systemMonitorService.getMonitoringData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("message", "获取监控数据成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取监控数据失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取监控数据失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> systemInfo = systemMonitorService.getSystemInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", systemInfo);
            response.put("message", "获取系统信息成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取系统信息失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取Redis信息
     */
    @GetMapping("/redis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        try {
            Map<String, Object> redisInfo = systemMonitorService.getRedisInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", redisInfo);
            response.put("message", "获取Redis信息成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取Redis信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取Redis信息失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取队列统计信息
     */
    @GetMapping("/queue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getQueueStats() {
        try {
            Map<String, Object> queueStats = systemMonitorService.getQueueStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", queueStats);
            response.put("message", "获取队列统计信息成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取队列统计信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取队列统计信息失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取队列统计信息（别名接口，用于首页统计）
     */
    @GetMapping("/queue-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getQueueStatsAlias() {
        return getQueueStats();
    }
    
    /**
     * 获取性能指标
     */
    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            Map<String, Object> performanceMetrics = systemMonitorService.getPerformanceMetrics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", performanceMetrics);
            response.put("message", "获取性能指标成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取性能指标失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取API统计信息
     */
    @GetMapping("/api-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getApiStats() {
        try {
            Map<String, Object> apiStats = systemMonitorService.getApiStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", apiStats);
            response.put("message", "获取API统计信息成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取API统计信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取API统计信息失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取历史数据
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getHistoryData(
            @RequestParam(defaultValue = "1h") String timeRange) {
        try {
            Map<String, Object> historyData = systemMonitorService.getHistoryData(timeRange);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", historyData);
            response.put("message", "获取历史数据成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取历史数据失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取历史数据失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 手动保存历史数据
     */
    @PostMapping("/save-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> saveHistoryData() {
        try {
            systemMonitorService.saveHistoryData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "保存历史数据成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存历史数据失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存历史数据失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 记录API请求（供内部调用）
     */
    @PostMapping("/record-request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> recordApiRequest(
            @RequestParam String endpoint,
            @RequestParam long responseTime,
            @RequestParam(defaultValue = "false") boolean isError) {
        try {
            systemMonitorService.recordApiRequest(endpoint, responseTime, isError);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "记录API请求成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("记录API请求失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "记录API请求失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取系统日志
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Object> logs = systemMonitorService.getLogs(page, size, level, keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", logs);
            response.put("message", "获取系统日志成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取系统日志失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取系统日志失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取系统健康状态摘要
     */
    @GetMapping("/health-summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        try {
            Map<String, Object> systemInfo = systemMonitorService.getSystemInfo();
            Map<String, Object> redisInfo = systemMonitorService.getRedisInfo();
            Map<String, Object> queueStats = systemMonitorService.getQueueStats();
            
            Map<String, Object> summary = new HashMap<>();
            
            // 系统状态
            double cpuUsage = (Double) systemInfo.getOrDefault("cpuUsage", 0.0);
            double memoryUsage = (Double) systemInfo.getOrDefault("memoryUsage", 0.0);
            String redisStatus = (String) redisInfo.getOrDefault("status", "DOWN");
            
            String overallStatus;
            if (cpuUsage > 90 || memoryUsage > 90 || "DOWN".equals(redisStatus)) {
                overallStatus = "CRITICAL";
            } else if (cpuUsage > 80 || memoryUsage > 80) {
                overallStatus = "WARNING";
            } else {
                overallStatus = "HEALTHY";
            }
            
            summary.put("status", overallStatus);
            summary.put("cpuUsage", cpuUsage);
            summary.put("memoryUsage", memoryUsage);
            summary.put("redisStatus", redisStatus);
            summary.put("totalTasks", queueStats.getOrDefault("totalTasks", 0L));
            summary.put("failedTasks", queueStats.getOrDefault("failedTasks", 0L));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", summary);
            response.put("message", "获取健康状态摘要成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取健康状态摘要失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取健康状态摘要失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 清空系统日志
     */
    @PostMapping("/clear-logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> clearLogs() {
        try {
            systemMonitorService.clearLogs();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "系统日志清空成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("清空系统日志失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清空系统日志失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}