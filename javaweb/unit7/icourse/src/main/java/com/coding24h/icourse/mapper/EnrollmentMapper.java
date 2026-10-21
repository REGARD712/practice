package com.coding24h.icourse.mapper;

import com.coding24h.icourse.entity.Course;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EnrollmentMapper {

    @Insert("INSERT INTO enrollment (student_id, course_id) VALUES (#{studentId}, #{courseId})")
    int enroll(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Delete("DELETE FROM enrollment WHERE student_id = #{studentId} AND course_id = #{courseId}")
    int unenroll(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.teacher_id,
                   c.create_time, u.username AS teacher_name, e.enroll_time
            FROM enrollment e
            JOIN course c ON e.course_id = c.id
            JOIN `user` u ON c.teacher_id = u.id
            WHERE e.student_id = #{studentId}
            ORDER BY e.enroll_time DESC
            """)
    @Results({
            @Result(property = "coverUrl", column = "cover_url"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);
}
