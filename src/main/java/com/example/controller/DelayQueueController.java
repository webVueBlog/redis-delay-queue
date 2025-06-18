package com.example.controller;

import com.example.queue.RedisDelayQueue;
import org.springframework.web.bind.annotation.*;

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
} 