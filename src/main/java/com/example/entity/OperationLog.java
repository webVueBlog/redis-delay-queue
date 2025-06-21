package com.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_action", columnList = "action"),
    @Index(name = "idx_module", columnList = "module"),
    @Index(name = "idx_level", columnList = "level"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_ip", columnList = "ip")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class OperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 操作用户名
     */
    @Column(name = "username", length = 100, nullable = false)
    private String username;
    
    /**
     * 操作类型
     * LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW, EXPORT, etc.
     */
    @Column(name = "action", length = 50, nullable = false)
    private String action;
    
    /**
     * 操作模块
     * USER, QUEUE, MENU, ORGANIZATION, SETTINGS, MONITOR, etc.
     */
    @Column(name = "module", length = 50, nullable = false)
    private String module;
    
    /**
     * 日志级别
     * INFO, WARN, ERROR, DEBUG
     */
    @Column(name = "level", length = 20, nullable = false)
    private String level;
    
    /**
     * 操作描述
     */
    @Column(name = "description", length = 1000)
    private String description;
    
    /**
     * 客户端IP地址
     */
    @Column(name = "ip", length = 45)
    private String ip;
    
    /**
     * 用户代理信息
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 操作耗时（毫秒）
     */
    @Column(name = "duration")
    private Long duration;
    
    /**
     * 请求数据（JSON格式）
     */
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;
    
    /**
     * 响应数据（JSON格式）
     */
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;
    
    /**
     * 操作时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 操作结果状态
     * SUCCESS, FAILED, PARTIAL
     */
    @Column(name = "status", length = 20)
    private String status;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    /**
     * 业务ID（关联的业务对象ID）
     */
    @Column(name = "business_id")
    private Long businessId;
    
    /**
     * 业务类型
     */
    @Column(name = "business_type", length = 50)
    private String businessType;
    
    /**
     * 操作前数据（JSON格式）
     */
    @Column(name = "old_data", columnDefinition = "TEXT")
    private String oldData;
    
    /**
     * 操作后数据（JSON格式）
     */
    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;
    
    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    /**
     * 请求URL
     */
    @Column(name = "request_url", length = 500)
    private String requestUrl;
    
    /**
     * 请求方法
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    /**
     * 操作来源
     * WEB, API, MOBILE, SYSTEM
     */
    @Column(name = "source", length = 20)
    private String source;
    
    /**
     * 租户ID（多租户场景）
     */
    @Column(name = "tenant_id")
    private Long tenantId;
    
    /**
     * 扩展字段1
     */
    @Column(name = "ext_field1", length = 200)
    private String extField1;
    
    /**
     * 扩展字段2
     */
    @Column(name = "ext_field2", length = 200)
    private String extField2;
    
    /**
     * 扩展字段3
     */
    @Column(name = "ext_field3", length = 200)
    private String extField3;
}