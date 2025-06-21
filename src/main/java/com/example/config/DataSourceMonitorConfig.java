package com.example.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;

/**
 * 数据源监控配置类
 * 用于监控HikariCP连接池的性能指标
 */
@Configuration
@EnableScheduling
public class DataSourceMonitorConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceMonitorConfig.class);

    private final DataSource primaryDataSource;
    private final DataSource userDataSource;

    public DataSourceMonitorConfig(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                 @Qualifier("userDataSource") DataSource userDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.userDataSource = userDataSource;
    }

    /**
     * 定时监控连接池状态
     * 每5分钟输出一次连接池状态信息
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void monitorConnectionPools() {
        try {
            logConnectionPoolStats("Primary", (HikariDataSource) primaryDataSource);
            logConnectionPoolStats("User", (HikariDataSource) userDataSource);
        } catch (Exception e) {
            logger.error("监控连接池状态时发生错误", e);
        }
    }

    /**
     * 记录连接池统计信息
     */
    private void logConnectionPoolStats(String poolName, HikariDataSource dataSource) {
        try {
            HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
            
            logger.info("=== {} 连接池状态 ===", poolName);
            logger.info("活跃连接数: {}", poolBean.getActiveConnections());
            logger.info("空闲连接数: {}", poolBean.getIdleConnections());
            logger.info("总连接数: {}", poolBean.getTotalConnections());
            logger.info("等待连接的线程数: {}", poolBean.getThreadsAwaitingConnection());
            logger.info("最大连接池大小: {}", dataSource.getMaximumPoolSize());
            logger.info("最小空闲连接数: {}", dataSource.getMinimumIdle());
            
            // 计算连接池使用率
            int activeConnections = poolBean.getActiveConnections();
            int maxPoolSize = dataSource.getMaximumPoolSize();
            double utilizationRate = (double) activeConnections / maxPoolSize * 100;
            logger.info("连接池使用率: {:.2f}%", utilizationRate);
            
            // 警告阈值检查
            if (utilizationRate > 80) {
                logger.warn("{} 连接池使用率过高: {:.2f}%, 建议检查应用性能", poolName, utilizationRate);
            }
            
            if (poolBean.getThreadsAwaitingConnection() > 0) {
                logger.warn("{} 连接池有 {} 个线程在等待连接，可能存在连接不足问题", 
                           poolName, poolBean.getThreadsAwaitingConnection());
            }
            
        } catch (Exception e) {
            logger.error("获取 {} 连接池统计信息失败", poolName, e);
        }
    }

    /**
     * 获取连接池健康状态
     */
    @Bean
    public ConnectionPoolHealthIndicator connectionPoolHealthIndicator() {
        return new ConnectionPoolHealthIndicator(primaryDataSource, userDataSource);
    }

    /**
     * 连接池健康检查指示器
     */
    public static class ConnectionPoolHealthIndicator {
        private final DataSource primaryDataSource;
        private final DataSource userDataSource;

        public ConnectionPoolHealthIndicator(DataSource primaryDataSource, DataSource userDataSource) {
            this.primaryDataSource = primaryDataSource;
            this.userDataSource = userDataSource;
        }

        /**
         * 检查连接池健康状态
         */
        public boolean isHealthy() {
            try {
                return checkDataSourceHealth((HikariDataSource) primaryDataSource) &&
                       checkDataSourceHealth((HikariDataSource) userDataSource);
            } catch (Exception e) {
                logger.error("检查连接池健康状态时发生错误", e);
                return false;
            }
        }

        private boolean checkDataSourceHealth(HikariDataSource dataSource) {
            try {
                HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
                
                // 检查连接池是否正常运行
                int totalConnections = poolBean.getTotalConnections();
                int activeConnections = poolBean.getActiveConnections();
                
                // 基本健康检查：总连接数应该大于0，活跃连接数不应该等于最大连接数
                return totalConnections > 0 && activeConnections < dataSource.getMaximumPoolSize();
                
            } catch (Exception e) {
                logger.error("检查数据源健康状态失败", e);
                return false;
            }
        }
    }
}