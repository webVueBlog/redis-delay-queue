package com.example.service;

import com.example.dto.DeviceDTO;
import com.example.entity.Device;
import com.example.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 设备服务层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String DEVICE_CACHE_PREFIX = "device:";
    private static final String DEVICE_HEARTBEAT_PREFIX = "device:heartbeat:";
    private static final String DEVICE_STATUS_PREFIX = "device:status:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）
    private static final long HEARTBEAT_TIMEOUT = 5; // 心跳超时时间（分钟）
    
    /**
     * 设备注册
     */
    @Transactional
    public DeviceDTO registerDevice(DeviceDTO.RegisterRequest request) {
        log.info("注册设备: {}", request.getDeviceId());
        
        // 检查设备是否已存在
        if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
            throw new RuntimeException("设备ID已存在: " + request.getDeviceId());
        }
        
        // 创建设备实体
        Device device = new Device();
        device.setDeviceId(request.getDeviceId());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setIpAddress(request.getIpAddress());
        device.setPort(request.getPort());
        device.setMacAddress(request.getMacAddress());
        device.setSerialNumber(request.getSerialNumber());
        device.setManufacturer(request.getManufacturer());
        device.setModel(request.getModel());
        device.setFirmwareVersion(request.getFirmwareVersion());
        device.setLocation(request.getLocation());
        device.setOrganizationId(request.getOrganizationId());
        device.setDescription(request.getDescription());
        device.setStatus(Device.DeviceStatus.OFFLINE);
        device.setEnabled(true);
        
        // 处理配置数据
        if (request.getConfigData() != null && !request.getConfigData().isEmpty()) {
            device.setConfigData(convertMapToJson(request.getConfigData()));
        }
        
        // 处理标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            device.setTags(String.join(",", request.getTags()));
        }
        
        // 保存设备
        Device savedDevice = deviceRepository.save(device);
        
        // 缓存设备信息
        cacheDevice(savedDevice);
        
        log.info("设备注册成功: {}", savedDevice.getDeviceId());
        return convertToDTO(savedDevice);
    }
    
    /**
     * 设备心跳
     */
    @Transactional
    public void deviceHeartbeat(DeviceDTO.HeartbeatRequest request) {
        log.debug("收到设备心跳: {}", request.getDeviceId());
        
        Device device = deviceRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("设备不存在: " + request.getDeviceId()));
        
        LocalDateTime now = LocalDateTime.now();
        Device.DeviceStatus oldStatus = device.getStatus();
        
        // 更新设备状态和心跳时间
        device.setStatus(request.getStatus());
        device.setLastHeartbeat(now);
        
        // 更新IP地址（如果提供）
        if (StringUtils.hasText(request.getIpAddress())) {
            device.setIpAddress(request.getIpAddress());
        }
        
        // 处理状态变化
        if (oldStatus != request.getStatus()) {
            if (request.getStatus() == Device.DeviceStatus.ONLINE) {
                device.setLastOnlineTime(now);
                log.info("设备上线: {}", device.getDeviceId());
            } else if (request.getStatus() == Device.DeviceStatus.OFFLINE) {
                device.setLastOfflineTime(now);
                log.info("设备离线: {}", device.getDeviceId());
            }
        }
        
        deviceRepository.save(device);
        
        // 更新Redis缓存
        updateDeviceCache(device);
        updateHeartbeatCache(device.getDeviceId(), now);
        
        log.debug("设备心跳处理完成: {}", request.getDeviceId());
    }
    
    /**
     * 获取设备信息
     */
    public DeviceDTO getDevice(String deviceId) {
        // 先从缓存获取
        Device cachedDevice = getCachedDevice(deviceId);
        if (cachedDevice != null) {
            return convertToDTO(cachedDevice);
        }
        
        // 从数据库获取
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("设备不存在: " + deviceId));
        
        // 缓存设备信息
        cacheDevice(device);
        
        return convertToDTO(device);
    }
    
    /**
     * 分页查询设备
     */
    public Page<DeviceDTO> queryDevices(DeviceDTO.QueryRequest request) {
        // 构建查询条件
        Specification<Device> spec = buildSpecification(request);
        
        // 构建分页和排序
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDirection()) ? 
            Sort.Direction.DESC : Sort.Direction.ASC,
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);
        
        // 执行查询
        Page<Device> devicePage = deviceRepository.findAll(spec, pageable);
        
        // 转换为DTO
        return devicePage.map(this::convertToDTO);
    }
    
    /**
     * 更新设备信息
     */
    @Transactional
    public DeviceDTO updateDevice(String deviceId, DeviceDTO deviceDTO) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("设备不存在: " + deviceId));
        
        // 更新设备信息
        if (StringUtils.hasText(deviceDTO.getDeviceName())) {
            device.setDeviceName(deviceDTO.getDeviceName());
        }
        if (StringUtils.hasText(deviceDTO.getDeviceType())) {
            device.setDeviceType(deviceDTO.getDeviceType());
        }
        if (StringUtils.hasText(deviceDTO.getIpAddress())) {
            device.setIpAddress(deviceDTO.getIpAddress());
        }
        if (deviceDTO.getPort() != null) {
            device.setPort(deviceDTO.getPort());
        }
        if (StringUtils.hasText(deviceDTO.getMacAddress())) {
            device.setMacAddress(deviceDTO.getMacAddress());
        }
        if (StringUtils.hasText(deviceDTO.getSerialNumber())) {
            device.setSerialNumber(deviceDTO.getSerialNumber());
        }
        if (StringUtils.hasText(deviceDTO.getManufacturer())) {
            device.setManufacturer(deviceDTO.getManufacturer());
        }
        if (StringUtils.hasText(deviceDTO.getModel())) {
            device.setModel(deviceDTO.getModel());
        }
        if (StringUtils.hasText(deviceDTO.getFirmwareVersion())) {
            device.setFirmwareVersion(deviceDTO.getFirmwareVersion());
        }
        if (StringUtils.hasText(deviceDTO.getLocation())) {
            device.setLocation(deviceDTO.getLocation());
        }
        if (deviceDTO.getOrganizationId() != null) {
            device.setOrganizationId(deviceDTO.getOrganizationId());
        }
        if (StringUtils.hasText(deviceDTO.getDescription())) {
            device.setDescription(deviceDTO.getDescription());
        }
        if (deviceDTO.getEnabled() != null) {
            device.setEnabled(deviceDTO.getEnabled());
        }
        
        // 处理配置数据
        if (deviceDTO.getConfigData() != null) {
            device.setConfigData(convertMapToJson(deviceDTO.getConfigData()));
        }
        
        // 处理标签
        if (deviceDTO.getTags() != null) {
            device.setTags(String.join(",", deviceDTO.getTags()));
        }
        
        Device savedDevice = deviceRepository.save(device);
        
        // 更新缓存
        updateDeviceCache(savedDevice);
        
        log.info("设备信息更新成功: {}", deviceId);
        return convertToDTO(savedDevice);
    }
    
    /**
     * 删除设备
     */
    @Transactional
    public void deleteDevice(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("设备不存在: " + deviceId));
        
        deviceRepository.delete(device);
        
        // 清除缓存
        clearDeviceCache(deviceId);
        
        log.info("设备删除成功: {}", deviceId);
    }
    
    /**
     * 获取设备统计信息
     */
    public DeviceDTO.Statistics getDeviceStatistics() {
        DeviceDTO.Statistics statistics = new DeviceDTO.Statistics();
        
        // 基本统计
        statistics.setTotalDevices(deviceRepository.countEnabledDevices());
        statistics.setOnlineDevices(deviceRepository.countByStatus(Device.DeviceStatus.ONLINE));
        statistics.setOfflineDevices(deviceRepository.countByStatus(Device.DeviceStatus.OFFLINE));
        statistics.setMaintenanceDevices(deviceRepository.countByStatus(Device.DeviceStatus.MAINTENANCE));
        statistics.setErrorDevices(deviceRepository.countByStatus(Device.DeviceStatus.ERROR));
        
        // 计算在线率
        if (statistics.getTotalDevices() > 0) {
            double onlineRate = (double) statistics.getOnlineDevices() / statistics.getTotalDevices() * 100;
            statistics.setOnlineRate(Math.round(onlineRate * 100.0) / 100.0);
        } else {
            statistics.setOnlineRate(0.0);
        }
        
        // 设备类型统计
        Map<String, Long> deviceTypeCount = deviceRepository.countByDeviceType().stream()
                .collect(Collectors.toMap(
                    arr -> (String) arr[0],
                    arr -> (Long) arr[1]
                ));
        statistics.setDeviceTypeCount(deviceTypeCount);
        
        // 组织统计
        Map<String, Long> organizationCount = deviceRepository.countByOrganization().stream()
                .collect(Collectors.toMap(
                    arr -> String.valueOf(arr[0]),
                    arr -> (Long) arr[1]
                ));
        statistics.setOrganizationCount(organizationCount);
        
        // 厂商统计
        Map<String, Long> manufacturerCount = deviceRepository.countByManufacturer().stream()
                .collect(Collectors.toMap(
                    arr -> (String) arr[0],
                    arr -> (Long) arr[1]
                ));
        statistics.setManufacturerCount(manufacturerCount);
        
        statistics.setStatisticsTime(LocalDateTime.now());
        
        return statistics;
    }
    
    /**
     * 检查心跳超时的设备
     */
    @Transactional
    public void checkHeartbeatTimeout() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(HEARTBEAT_TIMEOUT);
        List<Device> timeoutDevices = deviceRepository.findHeartbeatTimeoutDevices(timeoutTime);
        
        for (Device device : timeoutDevices) {
            if (device.getStatus() == Device.DeviceStatus.ONLINE) {
                device.setStatus(Device.DeviceStatus.OFFLINE);
                device.setLastOfflineTime(LocalDateTime.now());
                deviceRepository.save(device);
                
                // 更新缓存
                updateDeviceCache(device);
                
                log.warn("设备心跳超时，状态变更为离线: {}", device.getDeviceId());
            }
        }
    }
    
    /**
     * 构建查询条件
     */
    private Specification<Device> buildSpecification(DeviceDTO.QueryRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(request.getDeviceId())) {
                predicates.add(criteriaBuilder.like(root.get("deviceId"), "%" + request.getDeviceId() + "%"));
            }
            
            if (StringUtils.hasText(request.getDeviceName())) {
                predicates.add(criteriaBuilder.like(root.get("deviceName"), "%" + request.getDeviceName() + "%"));
            }
            
            if (StringUtils.hasText(request.getDeviceType())) {
                predicates.add(criteriaBuilder.equal(root.get("deviceType"), request.getDeviceType()));
            }
            
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            
            if (StringUtils.hasText(request.getIpAddress())) {
                predicates.add(criteriaBuilder.like(root.get("ipAddress"), "%" + request.getIpAddress() + "%"));
            }
            
            if (StringUtils.hasText(request.getManufacturer())) {
                predicates.add(criteriaBuilder.equal(root.get("manufacturer"), request.getManufacturer()));
            }
            
            if (StringUtils.hasText(request.getModel())) {
                predicates.add(criteriaBuilder.equal(root.get("model"), request.getModel()));
            }
            
            if (request.getOrganizationId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("organizationId"), request.getOrganizationId()));
            }
            
            if (request.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), request.getEnabled()));
            }
            
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                for (String tag : request.getTags()) {
                    predicates.add(criteriaBuilder.like(root.get("tags"), "%" + tag + "%"));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * 转换为DTO
     */
    private DeviceDTO convertToDTO(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setDeviceId(device.getDeviceId());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setStatus(device.getStatus());
        dto.setIpAddress(device.getIpAddress());
        dto.setPort(device.getPort());
        dto.setMacAddress(device.getMacAddress());
        dto.setSerialNumber(device.getSerialNumber());
        dto.setManufacturer(device.getManufacturer());
        dto.setModel(device.getModel());
        dto.setFirmwareVersion(device.getFirmwareVersion());
        dto.setLocation(device.getLocation());
        dto.setOrganizationId(device.getOrganizationId());
        dto.setDescription(device.getDescription());
        dto.setLastHeartbeat(device.getLastHeartbeat());
        dto.setLastOnlineTime(device.getLastOnlineTime());
        dto.setLastOfflineTime(device.getLastOfflineTime());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        dto.setCreatedBy(device.getCreatedBy());
        dto.setUpdatedBy(device.getUpdatedBy());
        dto.setEnabled(device.getEnabled());
        
        // 处理配置数据
        if (StringUtils.hasText(device.getConfigData())) {
            dto.setConfigData(convertJsonToMap(device.getConfigData()));
        }
        
        // 处理标签
        if (StringUtils.hasText(device.getTags())) {
            dto.setTags(Arrays.asList(device.getTags().split(",")));
        }
        
        // 扩展字段
        dto.setIsOnline(device.isOnline());
        dto.setIsHeartbeatTimeout(device.isHeartbeatTimeout());
        
        // 计算在线时长
        if (device.getLastOnlineTime() != null && device.isOnline()) {
            long minutes = java.time.Duration.between(device.getLastOnlineTime(), LocalDateTime.now()).toMinutes();
            dto.setOnlineDuration(minutes);
        }
        
        return dto;
    }
    
    // Redis缓存相关方法
    private void cacheDevice(Device device) {
        String key = DEVICE_CACHE_PREFIX + device.getDeviceId();
        redisTemplate.opsForValue().set(key, device, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
    }
    
    private Device getCachedDevice(String deviceId) {
        String key = DEVICE_CACHE_PREFIX + deviceId;
        return (Device) redisTemplate.opsForValue().get(key);
    }
    
    private void updateDeviceCache(Device device) {
        cacheDevice(device);
        
        // 更新状态缓存
        String statusKey = DEVICE_STATUS_PREFIX + device.getDeviceId();
        redisTemplate.opsForValue().set(statusKey, device.getStatus().name(), CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
    }
    
    private void updateHeartbeatCache(String deviceId, LocalDateTime heartbeatTime) {
        String key = DEVICE_HEARTBEAT_PREFIX + deviceId;
        redisTemplate.opsForValue().set(key, heartbeatTime, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
    }
    
    private void clearDeviceCache(String deviceId) {
        redisTemplate.delete(DEVICE_CACHE_PREFIX + deviceId);
        redisTemplate.delete(DEVICE_HEARTBEAT_PREFIX + deviceId);
        redisTemplate.delete(DEVICE_STATUS_PREFIX + deviceId);
    }
    
    // JSON转换工具方法
    private String convertMapToJson(Map<String, Object> map) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            log.error("Map转JSON失败", e);
            return "{}";
        }
    }
    
    private Map<String, Object> convertJsonToMap(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            log.error("JSON转Map失败", e);
            return new HashMap<>();
        }
    }
}