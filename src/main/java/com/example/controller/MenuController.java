package com.example.controller;

import com.example.entity.Menu;
import com.example.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Slf4j
public class MenuController {
    
    private final MenuService menuService;
    
    /**
     * 获取菜单树（管理员权限）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMenuTree() {
        try {
            List<Menu> menuTree = menuService.getMenuTree();
            return ResponseEntity.ok(createSuccessResponse("查询成功", menuTree));
        } catch (Exception e) {
            log.error("获取菜单树失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 分页查询菜单（管理员权限）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMenus(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String requiredRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Menu> menuPage = menuService.getMenus(name, code, status, requiredRole, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", menuPage.getContent());
            response.put("page", menuPage.getNumber());
            response.put("size", menuPage.getSize());
            response.put("totalElements", menuPage.getTotalElements());
            response.put("totalPages", menuPage.getTotalPages());
            response.put("first", menuPage.isFirst());
            response.put("last", menuPage.isLast());
            
            return ResponseEntity.ok(createSuccessResponse("查询成功", response));
        } catch (Exception e) {
            log.error("分页查询菜单失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取菜单详情（管理员权限）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMenuById(@PathVariable Long id) {
        try {
            Menu menu = menuService.getMenuById(id);
            return ResponseEntity.ok(createSuccessResponse("查询成功", menu));
        } catch (Exception e) {
            log.error("获取菜单详情失败: id={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 创建菜单（管理员权限）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createMenu(@Valid @RequestBody Menu menu) {
        try {
            Menu createdMenu = menuService.createMenu(menu);
            return ResponseEntity.ok(createSuccessResponse("菜单创建成功", createdMenu));
        } catch (Exception e) {
            log.error("创建菜单失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 更新菜单（管理员权限）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMenu(@PathVariable Long id, @Valid @RequestBody Menu menu) {
        try {
            Menu updatedMenu = menuService.updateMenu(id, menu);
            return ResponseEntity.ok(createSuccessResponse("菜单更新成功", updatedMenu));
        } catch (Exception e) {
            log.error("更新菜单失败: id={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 删除菜单（管理员权限）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        try {
            menuService.deleteMenu(id);
            return ResponseEntity.ok(createSuccessResponse("菜单删除成功", null));
        } catch (Exception e) {
            log.error("删除菜单失败: id={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 获取所有菜单（用于下拉选择）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllMenus() {
        try {
            List<Menu> menus = menuService.getAllMenus();
            return ResponseEntity.ok(createSuccessResponse("查询成功", menus));
        } catch (Exception e) {
            log.error("获取所有菜单失败: {}", e.getMessage());
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