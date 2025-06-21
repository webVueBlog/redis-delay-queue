package com.example.service;

import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private SystemMonitorService systemMonitorService;

    private static final String STATISTICS_KEY_PREFIX = "statistics:";
    private static final String DAILY_STATS_KEY = "daily_stats:";

    /**
     * 获取统计数据
     */
    public Map<String, Object> getStatistics(int page, int size, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 设置默认日期范围
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // 获取概览数据
        Map<String, Object> overview = getOverview();
        result.put("overview", overview);
        
        // 获取详细数据
        List<Map<String, Object>> details = getDetailStats(page, size, startDate, endDate);
        result.put("details", details);
        result.put("total", getTotalDetailCount(startDate, endDate));
        
        // 获取图表数据
        Map<String, Object> charts = getChartData(startDate, endDate);
        result.put("charts", charts);
        
        return result;
    }

    /**
     * 获取统计概览
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // 用户统计
            long totalUsers = userRepository.count();
            long todayUsers = getTodayNewUsers();
            long yesterdayUsers = getYesterdayNewUsers();
            int userGrowth = (int) (todayUsers - yesterdayUsers);
            
            overview.put("totalUsers", totalUsers);
            overview.put("userGrowth", userGrowth);
            
            // 任务统计
            Map<String, Object> queueStats = systemMonitorService.getQueueStats();
            long totalTasks = (Long) queueStats.getOrDefault("totalTasks", 0L);
            long completedTasks = (Long) queueStats.getOrDefault("completedTasks", 0L);
            long failedTasks = (Long) queueStats.getOrDefault("failedTasks", 0L);
            
            double successRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
            
            overview.put("totalTasks", totalTasks);
            overview.put("taskGrowth", getTaskGrowth());
            overview.put("successRate", Math.round(successRate * 100.0) / 100.0);
            overview.put("successRateChange", getSuccessRateChange());
            
            // 性能统计
            Map<String, Object> apiStats = systemMonitorService.getApiStats();
            double avgResponseTime = getAverageResponseTime(apiStats);
            
            overview.put("avgProcessTime", Math.round(avgResponseTime));
            overview.put("processTimeChange", getProcessTimeChange());
            
        } catch (Exception e) {
            log.error("获取统计概览失败", e);
            // 返回默认值
            overview.put("totalUsers", 0);
            overview.put("userGrowth", 0);
            overview.put("totalTasks", 0);
            overview.put("taskGrowth", 0);
            overview.put("successRate", 0.0);
            overview.put("successRateChange", 0.0);
            overview.put("avgProcessTime", 0);
            overview.put("processTimeChange", 0);
        }
        
        return overview;
    }

    /**
     * 获取详细统计数据
     */
    private List<Map<String, Object>> getDetailStats(int page, int size, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> details = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            Map<String, Object> dayStats = getDayStats(current);
            details.add(dayStats);
            current = current.plusDays(1);
        }
        
        // 分页处理
        int start = (page - 1) * size;
        int end = Math.min(start + size, details.size());
        
        if (start >= details.size()) {
            return new ArrayList<>();
        }
        
        return details.subList(start, end);
    }

    /**
     * 获取某一天的统计数据
     */
    private Map<String, Object> getDayStats(LocalDate date) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("date", date.toString());
        stats.put("newUsers", getNewUsersOnDate(date));
        stats.put("totalTasks", getTasksOnDate(date));
        stats.put("completedTasks", getCompletedTasksOnDate(date));
        stats.put("failedTasks", getFailedTasksOnDate(date));
        
        int totalTasks = (Integer) stats.get("totalTasks");
        int completedTasks = (Integer) stats.get("completedTasks");
        double successRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
        
        stats.put("avgProcessTime", getAvgProcessTimeOnDate(date));
        stats.put("peakConcurrency", getPeakConcurrencyOnDate(date));
        stats.put("errorRate", getErrorRateOnDate(date));
        
        return stats;
    }

    /**
     * 获取图表数据
     */
    private Map<String, Object> getChartData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> charts = new HashMap<>();
        
        // 用户增长趋势
        Map<String, Object> userGrowth = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current.toString());
            values.add(getNewUsersOnDate(current));
            current = current.plusDays(1);
        }
        
        userGrowth.put("dates", dates);
        userGrowth.put("values", values);
        charts.put("userGrowth", userGrowth);
        
        // 任务状态统计
        Map<String, Object> queueStats = systemMonitorService.getQueueStats();
        List<Map<String, Object>> taskStats = new ArrayList<>();
        taskStats.add(Map.of("name", "已完成", "value", queueStats.getOrDefault("completedTasks", 0L)));
        taskStats.add(Map.of("name", "处理中", "value", queueStats.getOrDefault("processingTasks", 0L)));
        taskStats.add(Map.of("name", "等待中", "value", queueStats.getOrDefault("pendingTasks", 0L)));
        taskStats.add(Map.of("name", "失败", "value", queueStats.getOrDefault("failedTasks", 0L)));
        charts.put("taskStats", taskStats);
        
        // 性能趋势
        Map<String, Object> performance = new HashMap<>();
        List<Integer> responseTime = new ArrayList<>();
        List<Integer> throughput = new ArrayList<>();
        
        current = startDate;
        while (!current.isAfter(endDate)) {
            responseTime.add(getAvgProcessTimeOnDate(current));
            throughput.add(getThroughputOnDate(current));
            current = current.plusDays(1);
        }
        
        performance.put("dates", dates);
        performance.put("responseTime", responseTime);
        performance.put("throughput", throughput);
        charts.put("performance", performance);
        
        return charts;
    }

    /**
     * 导出统计数据
     */
    public byte[] exportStatistics(LocalDate startDate, LocalDate endDate) throws Exception {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("统计数据");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"日期", "新增用户", "任务总数", "完成任务", "失败任务", "成功率(%)", "平均处理时间(ms)", "峰值并发", "错误率(%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // 填充数据
        List<Map<String, Object>> details = getDetailStats(1, Integer.MAX_VALUE, startDate, endDate);
        for (int i = 0; i < details.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, Object> data = details.get(i);
            
            row.createCell(0).setCellValue((String) data.get("date"));
            row.createCell(1).setCellValue((Integer) data.get("newUsers"));
            row.createCell(2).setCellValue((Integer) data.get("totalTasks"));
            row.createCell(3).setCellValue((Integer) data.get("completedTasks"));
            row.createCell(4).setCellValue((Integer) data.get("failedTasks"));
            row.createCell(5).setCellValue((Double) data.get("successRate"));
            row.createCell(6).setCellValue((Integer) data.get("avgProcessTime"));
            row.createCell(7).setCellValue((Integer) data.get("peakConcurrency"));
            row.createCell(8).setCellValue((Double) data.get("errorRate"));
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    // 辅助方法
    private long getTodayNewUsers() {
        LocalDate today = LocalDate.now();
        return userRepository.countByCreatedAtBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );
    }

    private long getYesterdayNewUsers() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return userRepository.countByCreatedAtBetween(
            yesterday.atStartOfDay(),
            yesterday.plusDays(1).atStartOfDay()
        );
    }

    private int getNewUsersOnDate(LocalDate date) {
        return (int) userRepository.countByCreatedAtBetween(
            date.atStartOfDay(),
            date.plusDays(1).atStartOfDay()
        );
    }

    private int getTaskGrowth() {
        // 模拟任务增长数据
        return ThreadLocalRandom.current().nextInt(-10, 50);
    }

    private double getSuccessRateChange() {
        // 模拟成功率变化
        return ThreadLocalRandom.current().nextDouble(-5.0, 5.0);
    }

    private int getProcessTimeChange() {
        // 模拟处理时间变化
        return ThreadLocalRandom.current().nextInt(-50, 100);
    }

    private double getAverageResponseTime(Map<String, Object> apiStats) {
        if (apiStats == null || apiStats.isEmpty()) {
            return 0.0;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> endpoints = (Map<String, Map<String, Object>>) apiStats.get("endpoints");
        if (endpoints == null || endpoints.isEmpty()) {
            return 0.0;
        }
        
        return endpoints.values().stream()
            .mapToDouble(stats -> (Double) stats.getOrDefault("avgResponseTime", 0.0))
            .average()
            .orElse(0.0);
    }

    private int getTasksOnDate(LocalDate date) {
        // 模拟每日任务数据
        return ThreadLocalRandom.current().nextInt(50, 200);
    }

    private int getCompletedTasksOnDate(LocalDate date) {
        int totalTasks = getTasksOnDate(date);
        return (int) (totalTasks * ThreadLocalRandom.current().nextDouble(0.7, 0.95));
    }

    private int getFailedTasksOnDate(LocalDate date) {
        int totalTasks = getTasksOnDate(date);
        int completedTasks = getCompletedTasksOnDate(date);
        return totalTasks - completedTasks;
    }

    private int getAvgProcessTimeOnDate(LocalDate date) {
        return ThreadLocalRandom.current().nextInt(100, 500);
    }

    private int getPeakConcurrencyOnDate(LocalDate date) {
        return ThreadLocalRandom.current().nextInt(10, 100);
    }

    private double getErrorRateOnDate(LocalDate date) {
        return ThreadLocalRandom.current().nextDouble(0.1, 5.0);
    }

    private int getThroughputOnDate(LocalDate date) {
        return ThreadLocalRandom.current().nextInt(50, 300);
    }

    private long getTotalDetailCount(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1)).count();
    }
}