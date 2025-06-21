package com.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import javax.persistence.*;


/**
 * 系统设置实体类
 */
@Entity
@Table(name = "system_settings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"category", "setting_key"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 设置分类
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    /**
     * 设置键
     */
    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;
    
    /**
     * 设置值
     */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;
    
    /**
     * 设置描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 数据类型 (STRING, INTEGER, BOOLEAN, JSON)
     */
    @Column(name = "data_type", length = 20)
    private String dataType;
    
    /**
     * 是否敏感数据（如密码等）
     */
    @Column(name = "is_sensitive")
    private Boolean isSensitive = false;
    
    /**
     * 是否可编辑
     */
    @Column(name = "is_editable")
    private Boolean isEditable = true;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 创建者
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    /**
     * 更新者
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 构造函数
     */
    public SystemSettings(String category, String settingKey, String settingValue, String description, String dataType) {
        this.category = category;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.dataType = dataType;
        this.isSensitive = false;
        this.isEditable = true;
    }
    
    /**
     * 构造函数（包含敏感标识）
     */
    public SystemSettings(String category, String settingKey, String settingValue, String description, String dataType, Boolean isSensitive) {
        this.category = category;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.dataType = dataType;
        this.isSensitive = isSensitive;
        this.isEditable = true;
    }
}