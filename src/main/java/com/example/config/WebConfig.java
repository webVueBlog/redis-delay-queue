package com.example.config;

import com.example.interceptor.MonitoringInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final MonitoringInterceptor monitoringInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加监控拦截器
        registry.addInterceptor(monitoringInterceptor)
                .addPathPatterns("/api/**") // 只拦截API请求
                .excludePathPatterns(
                    "/api/monitor/data",
                    "/api/monitor/performance", 
                    "/api/monitor/api-stats",
                    "/api/monitor/logs",
                    "/api/monitor/health-summary"
                ); // 只排除获取监控数据的接口，保留其他API接口的统计
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "http://localhost:*", 
                    "http://127.0.0.1:*",
                    "http://192.168.*.*:*",
                    "http://10.*.*.*:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}