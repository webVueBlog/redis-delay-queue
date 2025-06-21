package com.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "menus")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 100, message = "菜单名称长度不能超过100个字符")
    private String name;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "菜单编码不能为空")
    @Size(max = 50, message = "菜单编码长度不能超过50个字符")
    private String code;
    
    @Column(length = 200)
    @Size(max = 200, message = "菜单路径长度不能超过200个字符")
    private String path;
    
    @Column(length = 50)
    @Size(max = 50, message = "菜单图标长度不能超过50个字符")
    private String icon;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(nullable = false)
    private Integer status = 1; // 1-正常，0-禁用
    
    @Column(name = "required_role", length = 50)
    @Size(max = 50, message = "所需角色长度不能超过50个字符")
    private String requiredRole;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 子菜单列表（用于构建菜单树）
    @Transient
    private List<Menu> children;
}