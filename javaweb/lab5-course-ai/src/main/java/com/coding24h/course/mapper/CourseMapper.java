package com.coding24h.course.mapper;

import com.coding24h.course.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {
    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.price, c.teacher_id, c.create_time,
                   u.id AS teacher_user_id
            FROM course c JOIN `user` u ON u.id = c.teacher_id
            ORDER BY c.create_time DESC, c.id DESC
            """)
    @Results(id = "courseResult", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "coverUrl", column = "cover_url"),
            @Result(property = "price", column = "price"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "teacher", column = "teacher_user_id",
                    one = @One(select = "com.coding24h.course.mapper.UserMapper.findById"))
    })
    List<Course> findAll();

    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.price, c.teacher_id, c.create_time,
                   u.id AS teacher_user_id
            FROM course c JOIN `user` u ON u.id = c.teacher_id
            WHERE c.id = #{id}
            """)
    @ResultMap("courseResult")
    Course findById(Long id);

    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.price, c.teacher_id, c.create_time,
                   u.id AS teacher_user_id
            FROM course c JOIN `user` u ON u.id = c.teacher_id
            WHERE c.teacher_id = #{teacherId}
            ORDER BY c.create_time DESC, c.id DESC
            """)
    @ResultMap("courseResult")
    List<Course> findByTeacherId(Long teacherId);

    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.price, c.teacher_id, c.create_time,
                   t.id AS teacher_user_id
            FROM enrollment e
            JOIN course c ON c.id = e.course_id
            JOIN `user` t ON t.id = c.teacher_id
            WHERE e.student_id = #{studentId}
            ORDER BY e.enroll_time DESC
            """)
    @ResultMap("courseResult")
    List<Course> findByStudentId(Long studentId);

    @Insert("""
            INSERT INTO course (name, description, cover_url, price, teacher_id)
            VALUES (#{name}, #{description}, #{coverUrl}, #{price}, #{teacherId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Update("""
            UPDATE course
            SET name = #{name}, description = #{description}, cover_url = #{coverUrl},
                price = #{price}, teacher_id = #{teacherId}
            WHERE id = #{id}
            """)
    int update(Course course);

    @Delete("DELETE FROM course WHERE id = #{id}")
    int deleteById(Long id);
}
