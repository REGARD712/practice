package com.coding24h.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.coding24h.course.mapper")
public class CourseAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseAiApplication.class, args);
	}

}
