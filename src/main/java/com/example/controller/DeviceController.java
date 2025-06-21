package com.example.controller;

import com.example.dto.DeviceDTO;
import com.example.entity.Device;
import com.example.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备注册中心控制器
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 设备注册
     */
    @PostMapping("/register")
    public ResponseEntity<DeviceDTO> registerDevice(@Valid @RequestBody DeviceDTO.RegisterRequest request) {
        log.info("接收到设备注册请求: {}", request.getDeviceId());
        DeviceDTO deviceDTO = deviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceDTO);
    }

    /**
     * 设备心跳
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> deviceHeartbeat(@Valid @RequestBody DeviceDTO.HeartbeatRequest request) {
        log.debug("接收到设备心跳请求: {}", request.getDeviceId());
        deviceService.deviceHeartbeat(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "心跳处理成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取设备信息
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable String deviceId) {
        log.info("获取设备信息: {}", deviceId);
        DeviceDTO deviceDTO = deviceService.getDevice(deviceId);
        return ResponseEntity.ok(deviceDTO);
    }

    /**
     * 分页查询设备
     */
    @GetMapping
    public ResponseEntity<?> queryDevices(DeviceDTO.QueryRequest request) {
        log.info("查询设备列表: {}", request);
        return ResponseEntity.ok(deviceService.queryDevices(request));
    }

    /**
     * 更新设备信息
     */
    @PutMapping("/{deviceId}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceDTO deviceDTO) {
        log.info("更新设备信息: {}", deviceId);
        DeviceDTO updatedDevice = deviceService.updateDevice(deviceId, deviceDTO);
        return ResponseEntity.ok(updatedDevice);
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Map<String, Object>> deleteDevice(@PathVariable String deviceId) {
        log.info("删除设备: {}", deviceId);
        deviceService.deleteDevice(deviceId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "设备删除成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取设备统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<DeviceDTO.Statistics> getDeviceStatistics() {
        log.info("获取设备统计信息");
        DeviceDTO.Statistics statistics = deviceService.getDeviceStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 批量查询设备状态
     */
    @PostMapping("/status/batch")
    public ResponseEntity<?> batchQueryDeviceStatus(@RequestBody DeviceDTO.BatchStatusRequest request) {
        log.info("批量查询设备状态: {}", request.getDeviceIds().size());
        
        Map<String, Device.DeviceStatus> statusMap = new HashMap<>();
        for (String deviceId : request.getDeviceIds()) {
            try {
                DeviceDTO device = deviceService.getDevice(deviceId);
                statusMap.put(deviceId, device.getStatus());
            } catch (Exception e) {
                statusMap.put(deviceId, null);
            }
        }
        
        return ResponseEntity.ok(statusMap);
    }

    /**
     * 设备状态变更
     */
    @PutMapping("/{deviceId}/status")
    public ResponseEntity<DeviceDTO> updateDeviceStatus(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceDTO.StatusUpdateRequest request) {
        log.info("更新设备状态: {} -> {}", deviceId, request.getStatus());
        
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setStatus(request.getStatus());
        
        DeviceDTO updatedDevice = deviceService.updateDevice(deviceId, deviceDTO);
        return ResponseEntity.ok(updatedDevice);
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("设备控制器异常", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}