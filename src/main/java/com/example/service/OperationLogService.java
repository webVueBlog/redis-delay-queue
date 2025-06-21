package com.example.service;

import com.example.entity.OperationLog;
import com.example.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationLogService {
    
    private final OperationLogRepository operationLogRepository;
    
    /**
     * 获取操作日志列表
     */
    public Map<String, Object> getLogs(int page, int size, String username, String action, 
                                       String module, String level, String ip, String keyword,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        
        // 参数验证
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 构建查询条件
        Specification<OperationLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(username)) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
            }
            
            if (StringUtils.hasText(action)) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            
            if (StringUtils.hasText(module)) {
                predicates.add(criteriaBuilder.equal(root.get("module"), module));
            }
            
            if (StringUtils.hasText(level)) {
                predicates.add(criteriaBuilder.equal(root.get("level"), level));
            }
            
            if (StringUtils.hasText(ip)) {
                predicates.add(criteriaBuilder.like(root.get("ip"), "%" + ip + "%"));
            }
            
            if (StringUtils.hasText(keyword)) {
                Predicate descPredicate = criteriaBuilder.like(root.get("description"), "%" + keyword + "%");
                Predicate userAgentPredicate = criteriaBuilder.like(root.get("userAgent"), "%" + keyword + "%");
                predicates.add(criteriaBuilder.or(descPredicate, userAgentPredicate));
            }
            
            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            
            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询
        Page<OperationLog> logPage = operationLogRepository.findAll(spec, pageable);
        
        // 转换为Map格式
        List<Map<String, Object>> logs = logPage.getContent().stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
        
        // 获取统计信息
        Map<String, Object> stats = getLogStats(startTime, endTime);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        result.put("total", logPage.getTotalElements());
        result.put("totalPages", logPage.getTotalPages());
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("stats", stats);
        
        return result;
    }
    
    /**
     * 获取操作日志详情
     */
    public Map<String, Object> getLogDetail(Long id) {
        Optional<OperationLog> logOpt = operationLogRepository.findById(id);
        return logOpt.map(this::convertToDetailMap).orElse(null);
    }
    
    /**
     * 导出操作日志
     */
    public byte[] exportLogs(String username, String action, String module, String level, 
                            String ip, String keyword, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        
        // 构建查询条件
        Specification<OperationLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(username)) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
            }
            
            if (StringUtils.hasText(action)) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            
            if (StringUtils.hasText(module)) {
                predicates.add(criteriaBuilder.equal(root.get("module"), module));
            }
            
            if (StringUtils.hasText(level)) {
                predicates.add(criteriaBuilder.equal(root.get("level"), level));
            }
            
            if (StringUtils.hasText(ip)) {
                predicates.add(criteriaBuilder.like(root.get("ip"), "%" + ip + "%"));
            }
            
            if (StringUtils.hasText(keyword)) {
                Predicate descPredicate = criteriaBuilder.like(root.get("description"), "%" + keyword + "%");
                Predicate userAgentPredicate = criteriaBuilder.like(root.get("userAgent"), "%" + keyword + "%");
                predicates.add(criteriaBuilder.or(descPredicate, userAgentPredicate));
            }
            
            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            
            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 查询所有符合条件的日志
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<OperationLog> logs = operationLogRepository.findAll(spec, sort);
        
        // 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("操作日志");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "用户名", "操作类型", "模块", "日志级别", "描述", "IP地址", "用户代理", "耗时(ms)", "操作时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // 填充数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < logs.size(); i++) {
            Row row = sheet.createRow(i + 1);
            OperationLog log = logs.get(i);
            
            row.createCell(0).setCellValue(log.getId());
            row.createCell(1).setCellValue(log.getUsername());
            row.createCell(2).setCellValue(log.getAction());
            row.createCell(3).setCellValue(log.getModule());
            row.createCell(4).setCellValue(log.getLevel());
            row.createCell(5).setCellValue(log.getDescription());
            row.createCell(6).setCellValue(log.getIp());
            row.createCell(7).setCellValue(log.getUserAgent());
            row.createCell(8).setCellValue(log.getDuration());
            row.createCell(9).setCellValue(log.getCreatedAt().format(formatter));
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 清空操作日志
     */
    @Transactional
    public int clearLogs(LocalDateTime beforeTime) {
        if (beforeTime != null) {
            // 删除指定时间之前的日志
            return operationLogRepository.deleteByCreatedAtBefore(beforeTime);
        } else {
            // 删除所有日志
            long count = operationLogRepository.count();
            operationLogRepository.deleteAll();
            return (int) count;
        }
    }
    
    /**
     * 批量删除操作日志
     */
    @Transactional
    public int batchDeleteLogs(List<Long> ids) {
        List<OperationLog> logs = operationLogRepository.findAllById(ids);
        operationLogRepository.deleteAll(logs);
        return logs.size();
    }
    
    /**
     * 获取操作日志统计信息
     */
    public Map<String, Object> getLogStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总日志数
            long total = operationLogRepository.count();
            stats.put("total", total);
            
            // 今日日志数
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            long today = operationLogRepository.countByCreatedAtBetween(todayStart, todayEnd);
            stats.put("today", today);
            
            // 错误日志数
            long errors = operationLogRepository.countByLevel("ERROR");
            stats.put("errors", errors);
            
            // 活跃用户数（今日有操作的用户数）
            long activeUsers = operationLogRepository.countDistinctUsernameByCreatedAtBetween(todayStart, todayEnd);
            stats.put("activeUsers", activeUsers);
            
            // 如果指定了时间范围，计算该范围内的统计
            if (startTime != null && endTime != null) {
                long rangeTotal = operationLogRepository.countByCreatedAtBetween(startTime, endTime);
                long rangeErrors = operationLogRepository.countByLevelAndCreatedAtBetween("ERROR", startTime, endTime);
                long rangeUsers = operationLogRepository.countDistinctUsernameByCreatedAtBetween(startTime, endTime);
                
                stats.put("rangeTotal", rangeTotal);
                stats.put("rangeErrors", rangeErrors);
                stats.put("rangeUsers", rangeUsers);
            }
            
        } catch (Exception e) {
            log.error("获取操作日志统计信息失败", e);
            // 返回默认值
            stats.put("total", 0);
            stats.put("today", 0);
            stats.put("errors", 0);
            stats.put("activeUsers", 0);
        }
        
        return stats;
    }
    
    /**
     * 记录操作日志
     */
    @Transactional
    public void recordLog(String username, String action, String module, String level, 
                         String description, String ip, String userAgent, Long duration,
                         String requestData, String responseData) {
        try {
            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setAction(action);
            log.setModule(module);
            log.setLevel(level != null ? level : "INFO");
            log.setDescription(description);
            log.setIp(ip);
            log.setUserAgent(userAgent);
            log.setDuration(duration != null ? duration : 0L);
            log.setRequestData(requestData);
            log.setResponseData(responseData);
            log.setCreatedAt(LocalDateTime.now());
            
            operationLogRepository.save(log);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
    
    /**
     * 转换为Map格式
     */
    private Map<String, Object> convertToMap(OperationLog operationLog) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", operationLog.getId());
        map.put("username", operationLog.getUsername());
        map.put("action", operationLog.getAction());
        map.put("module", operationLog.getModule());
        map.put("level", operationLog.getLevel());
        map.put("description", operationLog.getDescription());
        map.put("ip", operationLog.getIp());
        map.put("userAgent", operationLog.getUserAgent());
        map.put("duration", operationLog.getDuration());
        map.put("createdAt", operationLog.getCreatedAt());
        return map;
    }
    
    /**
     * 转换为详细Map格式
     */
    private Map<String, Object> convertToDetailMap(OperationLog operationLog) {
        Map<String, Object> map = convertToMap(operationLog);
        map.put("requestData", operationLog.getRequestData());
        map.put("responseData", operationLog.getResponseData());
        return map;
    }
}