package com.coding24h.course.config;

import com.coding24h.course.web.RoleInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RoleInterceptor roleInterceptor;
    private final Path uploadDirectory;

    public WebConfig(RoleInterceptor roleInterceptor,
                     @Value("${file.upload-dir:uploads}") String uploadDirectory) {
        this.roleInterceptor = roleInterceptor;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/login", "/uploads/**", "/webjars/**", "/error");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDirectory.toUri().toString());
    }
}
