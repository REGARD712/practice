package com.coding24h.icourse.mapper;

import com.coding24h.icourse.entity.Course;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.teacher_id,
                   c.create_time, u.username AS teacher_name
            FROM course c JOIN `user` u ON c.teacher_id = u.id
            ORDER BY c.id
            """)
    @Results({
            @Result(property = "coverUrl", column = "cover_url"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Course> findAll();

    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.teacher_id,
                   c.create_time, u.username AS teacher_name
            FROM course c JOIN `user` u ON c.teacher_id = u.id
            WHERE c.id = #{id}
            """)
    @Results({
            @Result(property = "coverUrl", column = "cover_url"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "createTime", column = "create_time")
    })
    Course findById(@Param("id") Long id);

    @Insert("""
            INSERT INTO course (name, description, cover_url, teacher_id)
            VALUES (#{name}, #{description}, #{coverUrl}, #{teacherId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Update("""
            UPDATE course
            SET name = #{name}, description = #{description},
                cover_url = #{coverUrl}, teacher_id = #{teacherId}
            WHERE id = #{id}
            """)
    int update(Course course);

    @Delete("DELETE FROM course WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("""
            SELECT c.id, c.name, c.description, c.cover_url, c.teacher_id,
                   c.create_time, u.username AS teacher_name
            FROM course c JOIN `user` u ON c.teacher_id = u.id
            WHERE c.teacher_id = #{teacherId}
            ORDER BY c.id
            """)
    @Results({
            @Result(property = "coverUrl", column = "cover_url"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);
}
