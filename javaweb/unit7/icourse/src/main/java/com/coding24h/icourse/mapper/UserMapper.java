package com.coding24h.icourse.mapper;

import com.coding24h.icourse.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE id = #{id}")
    User findById(@Param("id") Long id);

    @Insert("INSERT INTO `user` (username, password, role) VALUES (#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE role = 'teacher'")
    List<User> findAllTeachers();
}
