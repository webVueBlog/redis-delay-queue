package com.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private Integer status;
    
    private String role;
    
    private List<Long> organizationIds;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginTime;
    
    // 登录请求DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }
    
    // 注册请求DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        private String username;
        
        @Email(message = "邮箱格式不正确")
        private String email;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码长度不能少于6个字符")
        private String password;
        
        private String role = "USER";
        
        private List<Long> organizationIds;
    }
    
    // 更新用户请求DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Email(message = "邮箱格式不正确")
        private String email;
        
        private Integer status;
        
        private String role;
        
        private List<Long> organizationIds;
    }
    
    // 修改密码请求DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;
        
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "新密码长度不能少于6个字符")
        private String newPassword;
    }
    
    // 重置密码请求DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "新密码长度不能少于6个字符")
        private String newPassword;
    }
    
    // 分页查询请求DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageRequest {
        private int page = 0;
        private int size = 10;
        private String username;
        private String email;
        private Integer status;
        private String role;
        private String sortBy = "createdAt";
        private String sortDir = "desc";
    }
    
    // 登录响应DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String tokenType = "Bearer";
        private UserDTO user;
        private List<String> menus;
    }
    
    // 分页响应DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResponse {
        private List<UserDTO> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}