package com.ningct.nowcodercommunity.config;

import com.ningct.nowcodercommunity.controller.interceptor.DataInterceptor;
import com.ningct.nowcodercommunity.controller.interceptor.LoginTicketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private DataInterceptor dataInterceptor;
    @Resource
    private LoginTicketInterceptor loginTicketInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.js","/**/*.css","/**/*.html","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.js","/**/*.css","/**/*.html","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
