package com.example.controller;

import com.example.dto.UserDTO;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDTO.LoginRequest request) {
        try {
            UserDTO.LoginResponse response = userService.login(request);
            log.info("用户登录成功: {}", request.getUsername());
            return ResponseEntity.ok(createSuccessResponse("登录成功", response));
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO.RegisterRequest request) {
        try {
            UserDTO user = userService.register(request);
            log.info("用户注册成功: {}", request.getUsername());
            return ResponseEntity.ok(createSuccessResponse("注册成功", user));
        } catch (Exception e) {
            log.error("用户注册失败: {}, 错误: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            UserDTO user = userService.getUserByUsername(username);
            return ResponseEntity.ok(createResponse(false, "用户名已存在", null));
        } catch (Exception e) {
            return ResponseEntity.ok(createResponse(true, "用户名可用", null));
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // 从token中提取用户名
            String jwt = token.substring(7); // 移除 "Bearer " 前缀
            // 这里需要从JWT中获取用户信息，暂时返回示例
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("message", "需要实现从JWT获取用户信息的逻辑");
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("获取用户信息失败"));
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT是无状态的，登出主要在前端清除token
        return ResponseEntity.ok(createSuccessResponse("登出成功", null));
    }
    
    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            // 实现token刷新逻辑
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token刷新功能待实现");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("刷新token失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("刷新token失败"));
        }
    }
    
    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        return createResponse(true, message, data);
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        return createResponse(false, message, null);
    }
    
    /**
     * 创建通用响应
     */
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
}