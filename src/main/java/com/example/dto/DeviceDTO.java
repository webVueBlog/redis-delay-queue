package com.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.entity.Device;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {
    
    private Long id;
    
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 100, message = "设备ID长度不能超过100个字符")
    private String deviceId;
    
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 200, message = "设备名称长度不能超过200个字符")
    private String deviceName;
    
    @NotBlank(message = "设备类型不能为空")
    @Size(max = 50, message = "设备类型长度不能超过50个字符")
    private String deviceType;
    
    @NotNull(message = "设备状态不能为空")
    private Device.DeviceStatus status;
    
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$", 
             message = "IP地址格式不正确")
    private String ipAddress;
    
    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号不能超过65535")
    private Integer port;
    
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", 
             message = "MAC地址格式不正确")
    private String macAddress;
    
    @Size(max = 100, message = "序列号长度不能超过100个字符")
    private String serialNumber;
    
    @Size(max = 100, message = "厂商名称长度不能超过100个字符")
    private String manufacturer;
    
    @Size(max = 100, message = "设备型号长度不能超过100个字符")
    private String model;
    
    @Size(max = 50, message = "固件版本长度不能超过50个字符")
    private String firmwareVersion;
    
    @Size(max = 200, message = "设备位置长度不能超过200个字符")
    private String location;
    
    private Long organizationId;
    
    @Size(max = 500, message = "设备描述长度不能超过500个字符")
    private String description;
    
    private Map<String, Object> configData;
    
    private List<String> tags;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOfflineTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    private Boolean enabled;
    
    // 扩展字段
    private String organizationName;
    private String createdByName;
    private String updatedByName;
    private Boolean isOnline;
    private Boolean isHeartbeatTimeout;
    private Long onlineDuration; // 在线时长（分钟）
    
    /**
     * 设备注册请求DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        
        @NotBlank(message = "设备ID不能为空")
        private String deviceId;
        
        @NotBlank(message = "设备名称不能为空")
        private String deviceName;
        
        @NotBlank(message = "设备类型不能为空")
        private String deviceType;
        
        private String ipAddress;
        private Integer port;
        private String macAddress;
        private String serialNumber;
        private String manufacturer;
        private String model;
        private String firmwareVersion;
        private String location;
        private Long organizationId;
        private String description;
        private Map<String, Object> configData;
        private List<String> tags;
    }
    
    /**
     * 设备心跳请求DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeartbeatRequest {
        
        @NotBlank(message = "设备ID不能为空")
        private String deviceId;
        
        @NotNull(message = "设备状态不能为空")
        private Device.DeviceStatus status;
        
        private String ipAddress;
        private Map<String, Object> statusData;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;
    }
    
    /**
     * 设备查询请求DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryRequest {
        
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private Device.DeviceStatus status;
        private String ipAddress;
        private String manufacturer;
        private String model;
        private Long organizationId;
        private List<String> tags;
        private Boolean enabled;
        
        // 分页参数
        @Min(value = 1, message = "页码必须大于0")
        private Integer page = 1;
        
        @Min(value = 1, message = "每页大小必须大于0")
        @Max(value = 100, message = "每页大小不能超过100")
        private Integer size = 20;
        
        // 排序参数
        private String sortBy = "createdAt";
        private String sortDirection = "desc";
    }
    
    /**
     * 设备统计DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        
        private Long totalDevices;
        private Long onlineDevices;
        private Long offlineDevices;
        private Long maintenanceDevices;
        private Long errorDevices;
        private Double onlineRate;
        private Map<String, Long> deviceTypeCount;
        private Map<String, Long> organizationCount;
        private Map<String, Long> manufacturerCount;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }
    
    /**
     * 批量状态查询请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchStatusRequest {
        @NotEmpty(message = "设备ID列表不能为空")
        private List<String> deviceIds;
    }
    
    /**
     * 状态更新请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull(message = "设备状态不能为空")
        private Device.DeviceStatus status;
    }
}