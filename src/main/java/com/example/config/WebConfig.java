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
                .excludePathPatterns("/api/monitor/**"); // 排除监控接口本身，避免循环
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}