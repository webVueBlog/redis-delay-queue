package com.example.config;

import com.example.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MonitoringScheduleConfig {
    
    private final SystemMonitorService systemMonitorService;
    
    /**
     * 每5分钟保存一次监控历史数据
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void saveMonitoringHistory() {
        try {
            log.debug("开始保存监控历史数据");
            systemMonitorService.saveHistoryData();
            log.debug("监控历史数据保存完成");
        } catch (Exception e) {
            log.error("保存监控历史数据失败", e);
        }
    }
    
    /**
     * 每小时清理过期的监控数据
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void cleanupExpiredData() {
        try {
            log.debug("开始清理过期监控数据");
            // 这里可以添加清理逻辑，比如删除超过7天的历史数据
            log.debug("过期监控数据清理完成");
        } catch (Exception e) {
            log.error("清理过期监控数据失败", e);
        }
    }
}