package com.example.controller;

import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserOrganizationController {
    
    private final UserService userService;
    
    /**
     * 获取指定用户的权限组织ID（管理员权限）
     */
    @GetMapping("/{userId}/organizations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserOrganizationIds(@PathVariable Long userId) {
        try {
            List<Long> organizationIds = userService.getUserOrganizationIds(userId);
            return ResponseEntity.ok(createSuccessResponse("查询成功", organizationIds));
        } catch (Exception e) {
            log.error("获取用户权限组织失败: userId={}, 错误: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}