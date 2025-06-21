package com.example.controller;

import com.example.entity.Organization;
import com.example.service.OrganizationService;
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
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    /**
     * 获取组织树（管理员权限）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrganizationTree() {
        try {
            List<Organization> organizationTree = organizationService.getOrganizationTree();
            return ResponseEntity.ok(createSuccessResponse("查询成功", organizationTree));
        } catch (Exception e) {
            log.error("获取组织树失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 分页查询组织（管理员权限）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrganizations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Organization> organizationPage = organizationService.getOrganizations(name, status, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", organizationPage.getContent());
            response.put("page", organizationPage.getNumber());
            response.put("size", organizationPage.getSize());
            response.put("totalElements", organizationPage.getTotalElements());
            response.put("totalPages", organizationPage.getTotalPages());
            response.put("first", organizationPage.isFirst());
            response.put("last", organizationPage.isLast());
            
            return ResponseEntity.ok(createSuccessResponse("查询成功", response));
        } catch (Exception e) {
            log.error("分页查询组织失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取组织详情（管理员权限）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrganizationById(@PathVariable Long id) {
        try {
            Organization organization = organizationService.getOrganizationById(id);
            return ResponseEntity.ok(createSuccessResponse("查询成功", organization));
        } catch (Exception e) {
            log.error("获取组织详情失败: id={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 创建组织（管理员权限）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createOrganization(@Valid @RequestBody Organization organization) {
        try {
            Organization createdOrganization = organizationService.createOrganization(organization);
            return ResponseEntity.ok(createSuccessResponse("组织创建成功", createdOrganization));
        } catch (Exception e) {
            log.error("创建组织失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 更新组织（管理员权限）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrganization(@PathVariable Long id, @Valid @RequestBody Organization organization) {
        try {
            Organization updatedOrganization = organizationService.updateOrganization(id, organization);
            return ResponseEntity.ok(createSuccessResponse("组织更新成功", updatedOrganization));
        } catch (Exception e) {
            log.error("更新组织失败: id={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 删除组织（管理员权限）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrganization(@PathVariable Long id) {
        try {
            organizationService.deleteOrganization(id);
            return ResponseEntity.ok(createSuccessResponse("组织删除成功", null));
        } catch (Exception e) {
            log.error("删除组织失败: id={}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 获取所有组织（用于下拉选择）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrganizations() {
        try {
            List<Organization> organizations = organizationService.getAllOrganizations();
            return ResponseEntity.ok(createSuccessResponse("查询成功", organizations));
        } catch (Exception e) {
            log.error("获取所有组织失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 根据父组织ID获取子组织
     */
    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getChildOrganizations(@PathVariable Long parentId) {
        try {
            List<Organization> children = organizationService.getChildOrganizations(parentId);
            return ResponseEntity.ok(createSuccessResponse("查询成功", children));
        } catch (Exception e) {
            log.error("获取子组织失败: parentId={}, 错误: {}", parentId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 获取组织及其所有子组织的ID列表
     */
    @GetMapping("/{id}/all-child-ids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllChildOrganizationIds(@PathVariable Long id) {
        try {
            List<Long> childIds = organizationService.getAllChildOrganizationIds(id);
            return ResponseEntity.ok(createSuccessResponse("查询成功", childIds));
        } catch (Exception e) {
            log.error("获取组织子ID列表失败: id={}, 错误: {}", id, e.getMessage());
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