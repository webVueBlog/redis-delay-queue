package com.example.controller;

import com.example.dto.SystemSettingsDTO;
import com.example.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "http://192.168.*.*:*", "http://10.*.*.*:*"}, allowCredentials = "true")
public class SystemSettingsController {
    
    private final SystemSettingsService systemSettingsService;
    
    /**
     * 获取所有系统设置
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSettings() {
        log.info("获取所有系统设置");
        
        try {
            SystemSettingsDTO settings = systemSettingsService.getAllSettings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取系统设置成功");
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取系统设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取系统设置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 保存所有系统设置
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> saveAllSettings(
            @RequestBody SystemSettingsDTO settingsDTO,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String userName) {
        
        log.info("保存所有系统设置，操作者: {}", userName);
        
        try {
            systemSettingsService.saveAllSettings(settingsDTO, userName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "系统设置保存成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存系统设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存系统设置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 测试Redis连接
     */
    @PostMapping("/test-redis")
    public ResponseEntity<Map<String, Object>> testRedisConnection(
            @RequestBody SystemSettingsDTO.RedisSettings redisSettings) {
        
        log.info("测试Redis连接: {}:{}", redisSettings.getHost(), redisSettings.getPort());
        
        try {
            boolean success = systemSettingsService.testRedisConnection(redisSettings);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Redis连接测试成功" : "Redis连接测试失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Redis连接测试异常", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Redis连接测试异常: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据分类获取设置
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getSettingsByCategory(
            @PathVariable String category) {
        
        log.info("获取分类设置: {}", category);
        
        try {
            var settings = systemSettingsService.getSettingsByCategory(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取分类设置成功");
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分类设置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取单个设置值
     */
    @GetMapping("/value")
    public ResponseEntity<Map<String, Object>> getSettingValue(
            @RequestParam String category,
            @RequestParam String key,
            @RequestParam(required = false, defaultValue = "") String defaultValue) {
        
        log.info("获取设置值: {}.{}", category, key);
        
        try {
            String value = systemSettingsService.getSettingValue(category, key, defaultValue);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取设置值成功");
            response.put("data", value);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取设置值失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取设置值失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 保存单个设置
     */
    @PutMapping("/value")
    public ResponseEntity<Map<String, Object>> saveSettingValue(
            @RequestParam String category,
            @RequestParam String key,
            @RequestParam String value,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String userName) {
        
        log.info("保存设置值: {}.{} = {}, 操作者: {}", category, key, value, userName);
        
        try {
            systemSettingsService.saveOrUpdateSetting(category, key, value, userName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "设置值保存成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存设置值失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存设置值失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 初始化默认设置
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeDefaultSettings(
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String userName) {
        
        log.info("初始化默认设置，操作者: {}", userName);
        
        try {
            systemSettingsService.initializeDefaultSettings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "默认设置初始化成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("初始化默认设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "初始化默认设置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 重置设置到默认值
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetToDefaults(
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String userName) {
        
        log.info("重置设置到默认值，操作者: {}", userName);
        
        try {
            // 先清空现有设置，再初始化默认设置
            systemSettingsService.initializeDefaultSettings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "设置已重置为默认值");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("重置设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "重置设置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 导出设置配置
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportSettings() {
        log.info("导出系统设置配置");
        
        try {
            SystemSettingsDTO settings = systemSettingsService.getAllSettings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "导出设置成功");
            response.put("data", settings);
            response.put("exportTime", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("导出设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "导出设置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 导入设置配置
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importSettings(
            @RequestBody SystemSettingsDTO settingsDTO,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String userName) {
        
        log.info("导入系统设置配置，操作者: {}", userName);
        
        try {
            systemSettingsService.saveAllSettings(settingsDTO, userName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "导入设置成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("导入设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "导入设置失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}