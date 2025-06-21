package com.example.controller;

import com.example.dto.UserDTO;
import com.example.entity.Menu;
import com.example.service.UserService;
import com.example.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    /**
     * 分页查询用户列表（管理员权限）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            UserDTO.PageRequest request = new UserDTO.PageRequest(
                    page, size, username, email, status, role, sortBy, sortDir
            );
            
            UserDTO.PageResponse response = userService.getUsers(request);
            return ResponseEntity.ok(createSuccessResponse("查询成功", response));
        } catch (Exception e) {
            log.error("查询用户列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).username == authentication.name")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(createSuccessResponse("查询成功", user));
        } catch (Exception e) {
            log.error("查询用户详情失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDTO user = userService.getUserByUsername(username);
            return ResponseEntity.ok(createSuccessResponse("查询成功", user));
        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).username == authentication.name")
    public ResponseEntity<?> updateUser(@PathVariable Long id, 
                                       @Valid @RequestBody UserDTO.UpdateRequest request) {
        try {
            UserDTO user = userService.updateUser(id, request);
            log.info("用户信息更新成功: ID={}", id);
            return ResponseEntity.ok(createSuccessResponse("更新成功", user));
        } catch (Exception e) {
            log.error("更新用户信息失败: ID={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 删除用户（管理员权限）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            log.info("用户删除成功: ID={}", id);
            return ResponseEntity.ok(createSuccessResponse("删除成功", null));
        } catch (Exception e) {
            log.error("删除用户失败: ID={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDTO currentUser = userService.getUserByUsername(username);
            
            userService.changePassword(currentUser.getId(), request);
            log.info("用户密码修改成功: {}", username);
            return ResponseEntity.ok(createSuccessResponse("密码修改成功", null));
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 重置密码（管理员权限）
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UserDTO.ResetPasswordRequest request) {
        try {
            userService.resetPassword(request);
            log.info("用户密码重置成功: {}", request.getUsername());
            return ResponseEntity.ok(createSuccessResponse("密码重置成功", null));
        } catch (Exception e) {
            log.error("重置密码失败: {}, 错误: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 获取用户菜单
     */
    @GetMapping("/menus")
    public ResponseEntity<?> getUserMenus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDTO user = userService.getUserByUsername(username);
            
            List<Menu> menus = userService.getUserMenus(user.getRole());
            return ResponseEntity.ok(createSuccessResponse("查询成功", menus));
        } catch (Exception e) {
            log.error("获取用户菜单失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 获取用户权限组织ID
     */
    @GetMapping("/organizations")
    public ResponseEntity<?> getUserOrganizations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDTO user = userService.getUserByUsername(username);
            
            List<Long> organizationIds = userService.getUserOrganizationIds(user.getId());
            return ResponseEntity.ok(createSuccessResponse("查询成功", organizationIds));
        } catch (Exception e) {
            log.error("获取用户组织权限失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 根据组织ID获取用户列表（管理员权限）
     */
    @GetMapping("/by-organization/{orgId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByOrganization(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // 这里需要实现根据组织ID查询用户的逻辑
            Map<String, Object> response = new HashMap<>();
            response.put("message", "根据组织ID查询用户功能待完善");
            response.put("orgId", orgId);
            return ResponseEntity.ok(createSuccessResponse("查询成功", response));
        } catch (Exception e) {
            log.error("根据组织ID查询用户失败: orgId={}, 错误: {}", orgId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 批量删除用户（管理员权限）
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> batchDeleteUsers(@RequestBody List<Long> userIds) {
        try {
            for (Long userId : userIds) {
                userService.deleteUser(userId);
            }
            log.info("批量删除用户成功: count={}", userIds.size());
            return ResponseEntity.ok(createSuccessResponse("批量删除成功", null));
        } catch (Exception e) {
            log.error("批量删除用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
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