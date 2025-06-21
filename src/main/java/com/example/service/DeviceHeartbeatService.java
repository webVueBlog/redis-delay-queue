package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 设备心跳检查服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceHeartbeatService {
    
    private final DeviceService deviceService;
    
    /**
     * 定时检查设备心跳超时
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void checkHeartbeatTimeout() {
        try {
            log.debug("开始检查设备心跳超时");
            deviceService.checkHeartbeatTimeout();
            log.debug("设备心跳超时检查完成");
        } catch (Exception e) {
            log.error("设备心跳超时检查失败", e);
        }
    }
}