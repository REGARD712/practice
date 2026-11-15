package com.petadoption.config;

import com.petadoption.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 的静态资源配置类。
 * 将浏览器访问的上传图片地址映射到本地文件系统，使数据库中的图片路径可以直接用于页面展示。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    /** 提供实际上传目录，保证资源映射位置与图片保存位置一致。 */
    private final FileStorageService fileStorageService;

    /**
     * 注册上传图片的静态资源处理器。
     *
     * @param registry Spring MVC 的资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将上传目录转换为 Spring 资源可识别的 file URI，例如 file:///.../uploads。
        String location = fileStorageService.getUploadDirectory().toUri().toString();
        // 资源目录必须以斜杠结尾，避免子文件路径被拼接到错误的父路径。
        if (!location.endsWith("/")) {
            location += "/";
        }
        // 把 /uploads/ 下的所有请求映射到实际上传目录，文件名由资源处理器负责解析。
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
