package com.example.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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