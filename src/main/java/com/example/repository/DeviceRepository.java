package com.example.repository;

import com.example.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备数据访问层
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    
    /**
     * 根据设备ID查找设备
     */
    Optional<Device> findByDeviceId(String deviceId);
    
    /**
     * 检查设备ID是否存在
     */
    boolean existsByDeviceId(String deviceId);
    
    /**
     * 根据设备类型查找设备
     */
    List<Device> findByDeviceType(String deviceType);
    
    /**
     * 根据设备状态查找设备
     */
    List<Device> findByStatus(Device.DeviceStatus status);
    
    /**
     * 根据组织ID查找设备
     */
    List<Device> findByOrganizationId(Long organizationId);
    
    /**
     * 根据IP地址查找设备
     */
    Optional<Device> findByIpAddress(String ipAddress);
    
    /**
     * 根据MAC地址查找设备
     */
    Optional<Device> findByMacAddress(String macAddress);
    
    /**
     * 根据序列号查找设备
     */
    Optional<Device> findBySerialNumber(String serialNumber);
    
    /**
     * 查找启用的设备
     */
    List<Device> findByEnabledTrue();
    
    /**
     * 查找禁用的设备
     */
    List<Device> findByEnabledFalse();
    
    /**
     * 根据设备名称模糊查询
     */
    Page<Device> findByDeviceNameContainingIgnoreCase(String deviceName, Pageable pageable);
    
    /**
     * 根据厂商查找设备
     */
    List<Device> findByManufacturer(String manufacturer);
    
    /**
     * 根据设备型号查找设备
     */
    List<Device> findByModel(String model);
    
    /**
     * 查找心跳超时的设备
     */
    @Query("SELECT d FROM Device d WHERE d.lastHeartbeat < :timeoutTime OR d.lastHeartbeat IS NULL")
    List<Device> findHeartbeatTimeoutDevices(@Param("timeoutTime") LocalDateTime timeoutTime);
    
    /**
     * 查找在线设备
     */
    @Query("SELECT d FROM Device d WHERE d.status = 'ONLINE' AND d.enabled = true")
    List<Device> findOnlineDevices();
    
    /**
     * 查找离线设备
     */
    @Query("SELECT d FROM Device d WHERE d.status = 'OFFLINE' AND d.enabled = true")
    List<Device> findOfflineDevices();
    
    /**
     * 统计设备总数
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.enabled = true")
    Long countEnabledDevices();
    
    /**
     * 根据状态统计设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status AND d.enabled = true")
    Long countByStatus(@Param("status") Device.DeviceStatus status);
    
    /**
     * 根据设备类型统计设备数量
     */
    @Query("SELECT d.deviceType, COUNT(d) FROM Device d WHERE d.enabled = true GROUP BY d.deviceType")
    List<Object[]> countByDeviceType();
    
    /**
     * 根据组织统计设备数量
     */
    @Query("SELECT d.organizationId, COUNT(d) FROM Device d WHERE d.enabled = true GROUP BY d.organizationId")
    List<Object[]> countByOrganization();
    
    /**
     * 根据厂商统计设备数量
     */
    @Query("SELECT d.manufacturer, COUNT(d) FROM Device d WHERE d.enabled = true AND d.manufacturer IS NOT NULL GROUP BY d.manufacturer")
    List<Object[]> countByManufacturer();
    
    /**
     * 查找指定时间范围内创建的设备
     */
    @Query("SELECT d FROM Device d WHERE d.createdAt BETWEEN :startTime AND :endTime")
    List<Device> findDevicesCreatedBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找指定时间范围内上线的设备
     */
    @Query("SELECT d FROM Device d WHERE d.lastOnlineTime BETWEEN :startTime AND :endTime")
    List<Device> findDevicesOnlineBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找长时间未上线的设备
     */
    @Query("SELECT d FROM Device d WHERE d.lastOnlineTime < :timeThreshold OR d.lastOnlineTime IS NULL")
    List<Device> findLongTimeOfflineDevices(@Param("timeThreshold") LocalDateTime timeThreshold);
    
    /**
     * 根据标签查找设备
     */
    @Query("SELECT d FROM Device d WHERE d.tags LIKE %:tag%")
    List<Device> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * 查找指定组织下的在线设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.organizationId = :organizationId AND d.status = 'ONLINE' AND d.enabled = true")
    Long countOnlineDevicesByOrganization(@Param("organizationId") Long organizationId);
    
    /**
     * 查找最近活跃的设备（按最后心跳时间排序）
     */
    @Query("SELECT d FROM Device d WHERE d.enabled = true ORDER BY d.lastHeartbeat DESC")
    Page<Device> findRecentActiveDevices(Pageable pageable);
    
    /**
     * 批量更新设备状态
     */
    @Query("UPDATE Device d SET d.status = :status, d.updatedAt = :updateTime WHERE d.deviceId IN :deviceIds")
    int batchUpdateStatus(@Param("deviceIds") List<String> deviceIds, 
                         @Param("status") Device.DeviceStatus status,
                         @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 删除指定时间之前的离线设备记录
     */
    @Query("DELETE FROM Device d WHERE d.status = 'OFFLINE' AND d.lastOfflineTime < :timeThreshold")
    int deleteOldOfflineDevices(@Param("timeThreshold") LocalDateTime timeThreshold);
}