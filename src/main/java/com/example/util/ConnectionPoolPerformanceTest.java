package com.example.util;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接池性能测试工具类
 * 用于测试和验证连接池优化效果
 */
@Component
public class ConnectionPoolPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolPerformanceTest.class);

    private final DataSource primaryDataSource;
    private final DataSource userDataSource;

    public ConnectionPoolPerformanceTest(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                       @Qualifier("userDataSource") DataSource userDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.userDataSource = userDataSource;
    }

    /**
     * 执行连接池性能测试
     * @param threadCount 并发线程数
     * @param operationsPerThread 每个线程执行的操作数
     * @param testDurationSeconds 测试持续时间（秒）
     */
    public void runPerformanceTest(int threadCount, int operationsPerThread, int testDurationSeconds) {
        logger.info("开始连接池性能测试 - 线程数: {}, 每线程操作数: {}, 测试时长: {}秒", 
                   threadCount, operationsPerThread, testDurationSeconds);

        // 测试主数据源
        testDataSourcePerformance("Primary", (HikariDataSource) primaryDataSource, 
                                 threadCount, operationsPerThread, testDurationSeconds);

        // 测试用户数据源
        testDataSourcePerformance("User", (HikariDataSource) userDataSource, 
                                 threadCount, operationsPerThread, testDurationSeconds);
    }

    /**
     * 测试单个数据源性能
     */
    private void testDataSourcePerformance(String dataSourceName, HikariDataSource dataSource,
                                          int threadCount, int operationsPerThread, int testDurationSeconds) {
        logger.info("=== 测试 {} 数据源性能 ===", dataSourceName);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

        List<Future<Void>> futures = new ArrayList<>();

        // 记录测试开始时间
        long startTime = System.currentTimeMillis();

        // 启动测试线程
        for (int i = 0; i < threadCount; i++) {
            Future<Void> future = executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    long operationStart = System.currentTimeMillis();
                    
                    try {
                        performDatabaseOperation(dataSource);
                        successCount.incrementAndGet();
                        
                        long responseTime = System.currentTimeMillis() - operationStart;
                        totalResponseTime.addAndGet(responseTime);
                        
                        // 更新最大响应时间
                        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
                        
                        // 更新最小响应时间
                        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        logger.debug("数据库操作失败", e);
                    }
                    
                    // 检查是否超过测试时间
                    if (System.currentTimeMillis() - startTime > testDurationSeconds * 1000L) {
                        break;
                    }
                }
                return null;
            });
            futures.add(future);
        }

        // 等待所有任务完成或超时
        try {
            for (Future<Void> future : futures) {
                future.get(testDurationSeconds + 10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("等待测试任务完成时发生错误", e);
        } finally {
            executor.shutdown();
        }

        // 计算测试结果
        long endTime = System.currentTimeMillis();
        long actualTestDuration = endTime - startTime;
        int totalOperations = successCount.get() + errorCount.get();
        double throughput = (double) successCount.get() / (actualTestDuration / 1000.0);
        double avgResponseTime = totalOperations > 0 ? (double) totalResponseTime.get() / totalOperations : 0;
        double errorRate = totalOperations > 0 ? (double) errorCount.get() / totalOperations * 100 : 0;

        // 输出测试结果
        logger.info("=== {} 数据源测试结果 ===", dataSourceName);
        logger.info("测试持续时间: {} ms", actualTestDuration);
        logger.info("成功操作数: {}", successCount.get());
        logger.info("失败操作数: {}", errorCount.get());
        logger.info("总操作数: {}", totalOperations);
        logger.info("吞吐量: {:.2f} ops/sec", throughput);
        logger.info("平均响应时间: {:.2f} ms", avgResponseTime);
        logger.info("最大响应时间: {} ms", maxResponseTime.get());
        logger.info("最小响应时间: {} ms", minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get());
        logger.info("错误率: {:.2f}%", errorRate);

        // 输出连接池状态
        logConnectionPoolStatus(dataSourceName, dataSource);
    }

    /**
     * 执行数据库操作
     */
    private void performDatabaseOperation(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // 执行简单的查询操作
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                }
            }
        }
    }

    /**
     * 记录连接池状态
     */
    private void logConnectionPoolStatus(String dataSourceName, HikariDataSource dataSource) {
        try {
            HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
            
            logger.info("=== {} 连接池状态 ===", dataSourceName);
            logger.info("活跃连接数: {}", poolBean.getActiveConnections());
            logger.info("空闲连接数: {}", poolBean.getIdleConnections());
            logger.info("总连接数: {}", poolBean.getTotalConnections());
            logger.info("等待连接的线程数: {}", poolBean.getThreadsAwaitingConnection());
            logger.info("最大连接池大小: {}", dataSource.getMaximumPoolSize());
            logger.info("最小空闲连接数: {}", dataSource.getMinimumIdle());
            
        } catch (Exception e) {
            logger.error("获取连接池状态失败", e);
        }
    }

    /**
     * 快速连接测试
     */
    public void quickConnectionTest() {
        logger.info("执行快速连接测试...");
        
        testConnection("Primary", primaryDataSource);
        testConnection("User", userDataSource);
    }

    /**
     * 测试单个连接
     */
    private void testConnection(String dataSourceName, DataSource dataSource) {
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection()) {
            long connectionTime = System.currentTimeMillis() - startTime;
            
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        long totalTime = System.currentTimeMillis() - startTime;
                        logger.info("{} 数据源连接测试成功 - 连接时间: {} ms, 总时间: {} ms", 
                                   dataSourceName, connectionTime, totalTime);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("{} 数据源连接测试失败", dataSourceName, e);
        }
    }

    /**
     * 连接池预热
     */
    public void warmUpConnectionPools() {
        logger.info("开始连接池预热...");
        
        warmUpDataSource("Primary", primaryDataSource);
        warmUpDataSource("User", userDataSource);
        
        logger.info("连接池预热完成");
    }

    /**
     * 预热单个数据源
     */
    private void warmUpDataSource(String dataSourceName, DataSource dataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        int minIdle = hikariDataSource.getMinimumIdle();
        
        logger.info("预热 {} 数据源，目标连接数: {}", dataSourceName, minIdle);
        
        List<Connection> connections = new ArrayList<>();
        
        try {
            // 创建最小空闲连接数的连接
            for (int i = 0; i < minIdle; i++) {
                Connection connection = dataSource.getConnection();
                connections.add(connection);
            }
            
            logger.info("{} 数据源预热完成，已创建 {} 个连接", dataSourceName, connections.size());
            
        } catch (SQLException e) {
            logger.error("{} 数据源预热失败", dataSourceName, e);
        } finally {
            // 关闭所有连接，让它们回到连接池
            for (Connection connection : connections) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.debug("关闭预热连接时发生错误", e);
                }
            }
        }
    }
}