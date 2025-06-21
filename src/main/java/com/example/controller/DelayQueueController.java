package com.example.controller;

import com.example.queue.RedisDelayQueue;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

@RestController
@RequestMapping("/api/delay-queue")
public class DelayQueueController {

    private final RedisDelayQueue delayQueue;

    public DelayQueueController(RedisDelayQueue delayQueue) {
        this.delayQueue = delayQueue;
    }

    @GetMapping("/add")
    public String addTask(@RequestParam String taskId, @RequestParam long delaySeconds) {
        delayQueue.addTask(taskId, delaySeconds);
        return "Task added successfully";
    }

    @PostMapping("/tasks")
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody TaskRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证必要字段
            if (request.getTaskType() == null || request.getTaskType().trim().isEmpty()) {
                throw new IllegalArgumentException("任务类型不能为空");
            }
            if (request.getTaskData() == null || request.getTaskData().trim().isEmpty()) {
                throw new IllegalArgumentException("任务数据不能为空");
            }
            if (request.getDelayTime() < 0) {
                throw new IllegalArgumentException("延迟时间不能为负数");
            }
            
            // 生成任务ID并添加到延迟队列
            String taskId = request.generateTaskId();
            delayQueue.addTask(taskId, request.getDelayTime());
            response.put("success", true);
            response.put("message", "任务创建成功");
            response.put("data", Map.of("taskId", taskId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 内部类用于接收请求参数
    public static class TaskRequest {
        private String queueName;
        private String taskType;
        private String taskData;
        private long delayTime;
        private String priority;
        private int maxRetries;
        
        public String getQueueName() { return queueName; }
        public void setQueueName(String queueName) { this.queueName = queueName; }
        
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        
        public String getTaskData() { return taskData; }
        public void setTaskData(String taskData) { this.taskData = taskData; }
        
        public long getDelayTime() { return delayTime; }
        public void setDelayTime(long delayTime) { this.delayTime = delayTime; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        
        // 生成唯一的任务ID
        public String generateTaskId() {
            return taskType + "_" + System.currentTimeMillis();
        }
     }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 从Redis获取所有任务
            Set<Object> allTasks = delayQueue.getAllPendingTasks();
            List<Map<String, Object>> taskList = new ArrayList<>();
            
            long currentTime = System.currentTimeMillis();
            for (Object taskObj : allTasks) {
                String taskId = taskObj.toString();
                Double executeTime = delayQueue.getTaskScore(taskId);
                
                // 过滤条件
                if (keyword != null && !keyword.trim().isEmpty() && !taskId.contains(keyword)) {
                    continue;
                }
                
                Map<String, Object> task = new HashMap<>();
                task.put("id", taskId);
                task.put("taskType", taskId.split("_")[0]); // 从taskId中提取类型
                task.put("executeTime", executeTime != null ? executeTime.longValue() : currentTime);
                task.put("status", executeTime != null && executeTime > currentTime ? "PENDING" : "READY");
                task.put("createTime", currentTime);
                task.put("priority", "NORMAL");
                taskList.add(task);
            }
            
            // 简单分页处理
            int totalElements = taskList.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size; // page从0开始，直接使用page * size
            int endIndex = Math.min(startIndex + size, totalElements);
            
            List<Map<String, Object>> pagedTasks = (startIndex < totalElements && startIndex >= 0) ? 
                taskList.subList(startIndex, endIndex) : new ArrayList<>();
            
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("content", pagedTasks);
            pageData.put("totalElements", totalElements);
            pageData.put("totalPages", totalPages);
            pageData.put("size", size);
            pageData.put("number", page); // 返回原始的page值
            
            response.put("success", true);
            response.put("data", pageData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取任务列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Long> stats = delayQueue.getTaskStats();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/tasks/{taskId}/retry")
    public ResponseEntity<Map<String, Object>> retryTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = delayQueue.retryTask(taskId);
            if (success) {
                response.put("success", true);
                response.put("message", "任务重试成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "任务不存在或重试失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "重试失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/tasks/{taskId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = delayQueue.cancelTask(taskId);
            if (success) {
                response.put("success", true);
                response.put("message", "任务取消成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "任务不存在或取消失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = delayQueue.deleteTask(taskId);
            if (success) {
                response.put("success", true);
                response.put("message", "任务删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "任务不存在或删除失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}