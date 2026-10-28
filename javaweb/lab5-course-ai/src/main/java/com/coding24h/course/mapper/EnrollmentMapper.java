package com.coding24h.course.mapper;

import com.coding24h.course.entity.Enrollment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EnrollmentMapper {
    @Insert("INSERT INTO enrollment (student_id, course_id) VALUES (#{studentId}, #{courseId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Enrollment enrollment);

    @Delete("DELETE FROM enrollment WHERE student_id = #{studentId} AND course_id = #{courseId}")
    int delete(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Select("SELECT id, student_id, course_id, enroll_time FROM enrollment WHERE student_id = #{studentId} ORDER BY enroll_time DESC")
    @Results(id = "enrollmentResult", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "enrollTime", column = "enroll_time"),
            @Result(property = "student", column = "student_id",
                    one = @One(select = "com.coding24h.course.mapper.UserMapper.findById")),
            @Result(property = "course", column = "course_id",
                    one = @One(select = "com.coding24h.course.mapper.CourseMapper.findById"))
    })
    List<Enrollment> findByStudentId(Long studentId);

    @Select("SELECT COUNT(*) FROM enrollment WHERE student_id = #{studentId} AND course_id = #{courseId}")
    boolean exists(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
