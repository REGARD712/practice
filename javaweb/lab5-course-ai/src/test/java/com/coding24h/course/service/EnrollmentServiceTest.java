package com.coding24h.course.service;

import com.coding24h.course.entity.Course;
import com.coding24h.course.mapper.CourseMapper;
import com.coding24h.course.mapper.EnrollmentMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EnrollmentServiceTest {
    @Test
    void preventsDuplicateEnrollment() {
        EnrollmentMapper enrollmentMapper = mock(EnrollmentMapper.class);
        CourseMapper courseMapper = mock(CourseMapper.class);
        when(courseMapper.findById(12L)).thenReturn(new Course());
        when(enrollmentMapper.exists(4L, 12L)).thenReturn(true);
        EnrollmentService service = new EnrollmentService(enrollmentMapper, courseMapper);

        assertThrows(IllegalArgumentException.class, () -> service.enroll(4L, 12L));
        verify(enrollmentMapper, never()).insert(any());
    }
}
