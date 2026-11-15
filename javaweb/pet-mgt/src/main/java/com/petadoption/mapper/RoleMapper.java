package com.petadoption.mapper;

import com.petadoption.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色表 role 的 MyBatis Mapper；SQL 使用反引号包裹 role，避免与数据库关键字冲突。
 */
public interface RoleMapper {
    /** 查询全部角色，按角色主键升序返回。 */
    @Select("SELECT id, role_name FROM `role` ORDER BY id")
    List<Role> findAll();

    /** 按角色主键查询单个角色，未找到时返回 null。 */
    @Select("SELECT id, role_name FROM `role` WHERE id = #{id}")
    Role findById(Long id);

    /** 按 role_name 精确查询角色，未找到时返回 null。 */
    @Select("SELECT id, role_name FROM `role` WHERE role_name = #{roleName}")
    Role findByRoleName(String roleName);

    /**
     * 查询指定用户拥有的全部角色。
     * 通过 user_role.role_id = role.id 内连接中间表，再按 user_id 过滤，结果按角色 ID 排序。
     * UserMapper 的 @Many 映射会调用此方法填充 User.roles。
     */
    @Select("""
            SELECT r.id, r.role_name
            FROM `role` r
            INNER JOIN user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
            ORDER BY r.id
            """)
    // 显式指定 MyBatis 参数名，使 SQL 中的 #{userId} 能准确绑定方法参数。
    List<Role> findByUserId(@Param("userId") Long userId);
}
