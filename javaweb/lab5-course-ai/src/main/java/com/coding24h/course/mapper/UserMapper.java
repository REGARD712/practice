package com.coding24h.course.mapper;

import com.coding24h.course.entity.User;
import com.coding24h.course.typehandler.RoleTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE username = #{username}")
    @Results(id = "userResult", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "role", column = "role", typeHandler = RoleTypeHandler.class),
            @Result(property = "createTime", column = "create_time")
    })
    User findByUsername(String username);

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE id = #{id}")
    @ResultMap("userResult")
    User findById(Long id);

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE role = #{role,typeHandler=com.coding24h.course.typehandler.RoleTypeHandler} ORDER BY id")
    @ResultMap("userResult")
    List<User> findByRole(@Param("role") User.Role role);

    @Select("SELECT id, username, password, role, create_time FROM `user` ORDER BY id")
    @ResultMap("userResult")
    List<User> findAll();

    @Insert("INSERT INTO `user` (username, password, role) VALUES (#{username}, #{password}, #{role,typeHandler=com.coding24h.course.typehandler.RoleTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE `user` SET username = #{username}, password = #{password}, role = #{role,typeHandler=com.coding24h.course.typehandler.RoleTypeHandler} WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM `user` WHERE id = #{id}")
    int deleteById(Long id);
}
