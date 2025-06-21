package com.example.interceptor;

import com.example.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringInterceptor implements HandlerInterceptor {
    
    private final SystemMonitorService systemMonitorService;
    
    private static final String START_TIME_ATTRIBUTE = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 计算响应时间
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                
                // 获取请求路径
                String endpoint = request.getRequestURI();
                
                // 判断是否为错误响应
                boolean isError = response.getStatus() >= 400 || ex != null;
                
                // 只记录API请求，排除静态资源
                if (endpoint.startsWith("/api/")) {
                    systemMonitorService.recordApiRequest(endpoint, responseTime, isError);
                }
            }
        } catch (Exception e) {
            log.warn("记录API监控数据失败", e);
        }
    }
}