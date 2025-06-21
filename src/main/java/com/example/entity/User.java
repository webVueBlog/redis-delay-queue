package com.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    @Column(length = 100)
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Integer status = 1; // 1-正常，0-禁用
    
    // 用户角色
    @Column(length = 50)
    private String role = "USER"; // USER, ADMIN
    
    // 组织ID列表，用逗号分隔
    @Column(name = "organization_ids", length = 500)
    private String organizationIds;
    
    // 最后登录时间
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // UserDetails 接口实现
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role);
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return status == 1;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return status == 1;
    }
    
    // 获取组织ID列表
    public List<Long> getOrganizationIdList() {
        if (organizationIds == null || organizationIds.trim().isEmpty()) {
            return List.of();
        }
        return List.of(organizationIds.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
    
    // 设置组织ID列表
    public void setOrganizationIdList(List<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) {
            this.organizationIds = null;
        } else {
            this.organizationIds = String.join(",", orgIds.stream().map(String::valueOf).collect(Collectors.toList()));
        }
    }
}