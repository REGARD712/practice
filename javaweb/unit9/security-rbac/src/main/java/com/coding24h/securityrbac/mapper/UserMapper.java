package com.coding24h.securityrbac.mapper;

import com.coding24h.securityrbac.entity.AppUser;
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
            @Result(property = "roles", column = "id", many = @Many(select = "findRoleNamesByUserId"))
    })
    AppUser findByUsername(String username);

    @Select("SELECT r.name FROM role r INNER JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> findRoleNamesByUserId(Long userId);

    @Insert("INSERT INTO app_user(username, password, enabled) VALUES(#{username}, #{password}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(AppUser user);

    @Insert("INSERT INTO user_role(user_id, role_id) VALUES(#{userId}, (SELECT id FROM role WHERE name = #{roleName}))")
    int insertRole(@Param("userId") Long userId, @Param("roleName") String roleName);
}
