package com.example.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import org.springframework.dao.DataAccessException;

@Slf4j
@Component
public class RedisDelayQueue {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService executorService;
    private static final String DELAY_QUEUE_KEY = "delay:queue";

    public RedisDelayQueue(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.executorService = Executors.newSingleThreadExecutor();//使用单线程池，避免并发问题
    }

    @PostConstruct
    public void init() {
        startDelayQueueConsumer();
    }

    /**
     * 添加延迟任务
     * @param taskId 任务ID
     * @param delay 延迟时间（秒）
     */
    public void addTask(String taskId, long delay) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("Delay time cannot be negative");
        }
        
        try {
            long executeTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delay);
            redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, taskId, executeTime);
            log.info("Task {} added to delay queue, will execute at {}", taskId, executeTime);
        } catch (DataAccessException e) {
            log.error("Failed to add task {} to delay queue: {}", taskId, e.getMessage());
            throw new RuntimeException("Redis connection error", e);
        }
    }

    /**
     * 启动延迟队列消费者
     */
    private void startDelayQueueConsumer() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 获取当前时间
                    long now = System.currentTimeMillis();
                    // 获取到期的任务
                    Set<Object> tasks = null;
                    try {
                        tasks = redisTemplate.opsForZSet().rangeByScore(DELAY_QUEUE_KEY, 0, now);
                    } catch (DataAccessException e) {
                        log.error("Failed to fetch tasks from delay queue: {}", e.getMessage());
                        Thread.sleep(5000); // 等待5秒后重试
                        continue;
                    }
                    
                    if (tasks != null && !tasks.isEmpty()) {
                        for (Object task : tasks) {
                            try {
                                // 处理任务
                                processTask(task.toString());
                                // 从队列中移除任务
                                redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, task);
                            } catch (DataAccessException e) {
                                log.error("Failed to process or remove task {}: {}", task, e.getMessage());
                            }
                        }
                    }
                    
                    // 休眠100ms，避免过于频繁的轮询
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error processing delay queue", e);
                }
            }
        });
    }

    /**
     * 处理任务
     * @param taskId 任务ID
     */
    private void processTask(String taskId) {
        log.info("Processing task: {}", taskId);
        // 这里可以添加具体的任务处理逻辑
    }
    
    /**
     * 获取所有待执行的任务
     * @return 任务列表
     */
    public Set<Object> getAllPendingTasks() {
        try {
            return redisTemplate.opsForZSet().range(DELAY_QUEUE_KEY, 0, -1);
        } catch (DataAccessException e) {
            log.error("Failed to get pending tasks: {}", e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * 获取任务统计信息
     * @return 统计信息
     */
    public Map<String, Long> getTaskStats() {
        Map<String, Long> stats = new HashMap<>();
        try {
            Long totalTasks = redisTemplate.opsForZSet().count(DELAY_QUEUE_KEY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            long now = System.currentTimeMillis();
            Long pendingTasks = redisTemplate.opsForZSet().count(DELAY_QUEUE_KEY, now, Double.POSITIVE_INFINITY);
            Long readyTasks = redisTemplate.opsForZSet().count(DELAY_QUEUE_KEY, Double.NEGATIVE_INFINITY, now);
            
            stats.put("total", totalTasks != null ? totalTasks : 0L);
            stats.put("pending", pendingTasks != null ? pendingTasks : 0L);
            stats.put("ready", readyTasks != null ? readyTasks : 0L);
            stats.put("running", 0L); // 暂时设为0，可以后续扩展
            stats.put("completed", 0L); // 暂时设为0，可以后续扩展
            stats.put("failed", 0L); // 暂时设为0，可以后续扩展
        } catch (DataAccessException e) {
            log.error("Failed to get task stats: {}", e.getMessage());
            stats.put("total", 0L);
            stats.put("pending", 0L);
            stats.put("ready", 0L);
            stats.put("running", 0L);
            stats.put("completed", 0L);
            stats.put("failed", 0L);
        }
        return stats;
    }
    
    /**
     * 获取任务详细信息
     * @param taskId 任务ID
     * @return 任务执行时间，如果任务不存在返回null
     */
    public Double getTaskScore(String taskId) {
        try {
            return redisTemplate.opsForZSet().score(DELAY_QUEUE_KEY, taskId);
        } catch (DataAccessException e) {
            log.error("Failed to get task score for {}: {}", taskId, e.getMessage());
            return null;
        }
    }
    
    /**
     * 重试任务
     * @param taskId 任务ID
     * @return 是否成功
     */
    public boolean retryTask(String taskId) {
        try {
            Double score = getTaskScore(taskId);
            if (score == null) {
                return false; // 任务不存在
            }
            
            // 设置为5秒后执行
            long newExecuteTime = System.currentTimeMillis() + 5000;
            redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, taskId, newExecuteTime);
            log.info("Task {} rescheduled to {}", taskId, newExecuteTime);
            return true;
        } catch (DataAccessException e) {
            log.error("Failed to retry task {}: {}", taskId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 取消任务
     * @param taskId 任务ID
     * @return 是否成功
     */
    public boolean cancelTask(String taskId) {
        try {
            Double score = getTaskScore(taskId);
            if (score == null) {
                return false; // 任务不存在
            }
            
            // 设置为永不执行（很远的将来）
            redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, taskId, Double.MAX_VALUE);
            log.info("Task {} cancelled", taskId);
            return true;
        } catch (DataAccessException e) {
            log.error("Failed to cancel task {}: {}", taskId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除任务
     * @param taskId 任务ID
     * @return 是否成功
     */
    public boolean deleteTask(String taskId) {
        try {
            Long removed = redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, taskId);
            boolean success = removed != null && removed > 0;
            if (success) {
                log.info("Task {} deleted", taskId);
            } else {
                log.warn("Task {} not found for deletion", taskId);
            }
            return success;
        } catch (DataAccessException e) {
            log.error("Failed to delete task {}: {}", taskId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 优雅关闭
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down delay queue consumer...");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("Delay queue consumer shutdown completed");
    }
}