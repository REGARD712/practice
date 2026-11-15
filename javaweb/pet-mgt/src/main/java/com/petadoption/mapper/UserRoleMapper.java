package com.petadoption.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 用户—角色中间表 user_role 的 MyBatis Mapper，仅维护两类主键之间的关联。
 */
public interface UserRoleMapper {
    /** 插入一条用户与角色的关联记录，返回受影响行数。 */
    @Insert("INSERT INTO user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    // @Param 分别绑定 SQL 中的 userId 和 roleId，避免多参数场景下名称不明确。
    int insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /** 删除指定用户的全部角色关联，返回实际删除的中间表行数。 */
    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
