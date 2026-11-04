package com.coding24h.securitydbauth.mapper;

import com.coding24h.securitydbauth.entity.AppUser;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM app_user WHERE username = #{username}")
    @Results(id = "userResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "roles", column = "id", many = @Many(select = "findRolesByUserId"))
    })
    AppUser findByUsername(String username);

    @Select("SELECT role FROM user_roles WHERE user_id = #{userId}")
    List<String> findRolesByUserId(Long userId);

    @Insert("INSERT INTO app_user(username, password, enabled) VALUES(#{username}, #{password}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(AppUser user);

    @Insert("INSERT INTO user_roles(user_id, role) VALUES(#{userId}, #{role})")
    int insertRole(@Param("userId") Long userId, @Param("role") String role);
}