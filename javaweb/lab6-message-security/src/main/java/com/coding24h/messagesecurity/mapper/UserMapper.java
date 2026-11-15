package com.coding24h.messagesecurity.mapper;

import java.util.List;

import com.coding24h.messagesecurity.entity.User;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM `user` WHERE username = #{username}")
    @Results(id = "userResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "roles", column = "id", many = @Many(select = "findRoleNamesByUserId"))
    })
    User findByUsername(@Param("username") String username);

    @Select("SELECT r.name FROM `role` r INNER JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> findRoleNamesByUserId(Long userId);

    @Insert("INSERT INTO `user`(username, password, enabled) VALUES(#{username}, #{password}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Insert("INSERT INTO user_role(user_id, role_id) VALUES(#{userId}, (SELECT id FROM `role` WHERE name = #{roleName}))")
    int insertRole(@Param("userId") Long userId, @Param("roleName") String roleName);

    @Select("SELECT id, username, enabled FROM `user`")
    List<User> findAllUsers();
}