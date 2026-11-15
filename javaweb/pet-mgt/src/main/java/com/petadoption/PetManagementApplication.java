package com.petadoption;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宠物领养管理系统的启动入口。
 * 组合注解启用 Spring Boot 自动配置、组件扫描及配置属性支持，MapperScan 则负责注册数据访问层接口。
 */
@SpringBootApplication
@MapperScan("com.petadoption.mapper")
public class PetManagementApplication {

    /**
     * 启动整个 Web 应用。
     *
     * @param args 启动时传入的命令行参数，可覆盖 Spring Boot 配置
     */
    public static void main(String[] args) {
        // 创建 Spring 应用上下文、完成 Bean 装配，并启动内嵌 Web 服务器。
        SpringApplication.run(PetManagementApplication.class, args);
    }
}
