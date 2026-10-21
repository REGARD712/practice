package com.coding24h.icourse.service;

import com.coding24h.icourse.entity.Course;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CourseService {

    List<Course> findAll();

    Course findById(Long id);

    Course add(Course course, MultipartFile cover) throws IOException;

    boolean update(Long id, Course course, MultipartFile cover) throws IOException;

    boolean delete(Long id);
}
