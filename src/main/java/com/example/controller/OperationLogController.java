package com.example.controller;

import com.example.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
@Slf4j
public class OperationLogController {
    
    private final OperationLogService operationLogService;
    
    /**
     * 获取操作日志列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Map<String, Object> result = operationLogService.getLogs(
                page, size, username, action, module, level, ip, keyword, startTime, endTime
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", "获取操作日志成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取操作日志失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取操作日志失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取操作日志详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getLogDetail(@PathVariable Long id) {
        try {
            Map<String, Object> logDetail = operationLogService.getLogDetail(id);
            
            if (logDetail == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "日志不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", logDetail);
            response.put("message", "获取日志详情成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取日志详情失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取日志详情失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 导出操作日志
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            byte[] excelData = operationLogService.exportLogs(
                username, action, module, level, ip, keyword, startTime, endTime
            );
            
            String fileName = "operation_logs_" + System.currentTimeMillis() + ".xlsx";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
        } catch (Exception e) {
            log.error("导出操作日志失败", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 清空操作日志
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> clearLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        try {
            int deletedCount = operationLogService.clearLogs(beforeTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "操作日志清空成功，共删除 " + deletedCount + " 条记录");
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("清空操作日志失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清空操作日志失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 批量删除操作日志
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> batchDeleteLogs(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Long> ids = (java.util.List<Long>) request.get("ids");
            
            if (ids == null || ids.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请选择要删除的日志");
                return ResponseEntity.badRequest().body(response);
            }
            
            int deletedCount = operationLogService.batchDeleteLogs(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批量删除成功，共删除 " + deletedCount + " 条记录");
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量删除操作日志失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量删除操作日志失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取操作日志统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getLogStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Map<String, Object> stats = operationLogService.getLogStats(startTime, endTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "获取统计信息成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取操作日志统计信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            response.put("error", true);
            
            return ResponseEntity.status(500).body(response);
        }
    }
}