package com.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备实体类
 */
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id", unique = true),
    @Index(name = "idx_device_type", columnList = "device_type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_organization_id", columnList = "organization_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 设备唯一标识
     */
    @Column(name = "device_id", nullable = false, unique = true, length = 100)
    private String deviceId;
    
    /**
     * 设备名称
     */
    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;
    
    /**
     * 设备类型
     */
    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;
    
    /**
     * 设备状态：ONLINE-在线, OFFLINE-离线, MAINTENANCE-维护中, ERROR-故障
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;
    
    /**
     * 设备IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * 设备端口
     */
    @Column(name = "port")
    private Integer port;
    
    /**
     * 设备MAC地址
     */
    @Column(name = "mac_address", length = 17)
    private String macAddress;
    
    /**
     * 设备序列号
     */
    @Column(name = "serial_number", length = 100)
    private String serialNumber;
    
    /**
     * 设备厂商
     */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;
    
    /**
     * 设备型号
     */
    @Column(name = "model", length = 100)
    private String model;
    
    /**
     * 固件版本
     */
    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;
    
    /**
     * 设备位置
     */
    @Column(name = "location", length = 200)
    private String location;
    
    /**
     * 所属组织ID
     */
    @Column(name = "organization_id")
    private Long organizationId;
    
    /**
     * 设备描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 设备配置信息（JSON格式）
     */
    @Column(name = "config_data", columnDefinition = "TEXT")
    private String configData;
    
    /**
     * 设备标签（逗号分隔）
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    /**
     * 最后上线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_online_time")
    private LocalDateTime lastOnlineTime;
    
    /**
     * 最后离线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_offline_time")
    private LocalDateTime lastOfflineTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 创建者ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
    /**
     * 更新者ID
     */
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DeviceStatus.OFFLINE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设备状态枚举
     */
    public enum DeviceStatus {
        ONLINE("在线"),
        OFFLINE("离线"),
        MAINTENANCE("维护中"),
        ERROR("故障");
        
        private final String description;
        
        DeviceStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 检查设备是否在线
     */
    public boolean isOnline() {
        return DeviceStatus.ONLINE.equals(this.status);
    }
    
    /**
     * 检查设备心跳是否超时（5分钟）
     */
    public boolean isHeartbeatTimeout() {
        if (lastHeartbeat == null) {
            return true;
        }
        return lastHeartbeat.isBefore(LocalDateTime.now().minusMinutes(5));
    }
}