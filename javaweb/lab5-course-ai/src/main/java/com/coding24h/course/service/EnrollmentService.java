package com.coding24h.course.service;

import com.coding24h.course.entity.Course;
import com.coding24h.course.entity.Enrollment;
import com.coding24h.course.mapper.CourseMapper;
import com.coding24h.course.mapper.EnrollmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;

    public EnrollmentService(EnrollmentMapper enrollmentMapper, CourseMapper courseMapper) {
        this.enrollmentMapper = enrollmentMapper;
        this.courseMapper = courseMapper;
    }

    @Transactional
    public void enroll(Long studentId, Long courseId) {
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (enrollmentMapper.exists(studentId, courseId)) {
            throw new IllegalArgumentException("你已经选过这门课程");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollmentMapper.insert(enrollment);
    }

    @Transactional
    public void withdraw(Long studentId, Long courseId) {
        if (enrollmentMapper.delete(studentId, courseId) == 0) {
            throw new IllegalArgumentException("未找到对应的选课记录");
        }
    }

    public List<Enrollment> findByStudent(Long studentId) {
        return enrollmentMapper.findByStudentId(studentId);
    }
}
