package com.petadoption.entity;

import lombok.Data;

/**
 * 用户角色实体，对应数据库 role 表。
 * Lombok 的 @Data 自动生成常用访问器和对象方法，无需手写重复代码。
 */
@Data
public class Role {
    /** 角色主键，对应 id 列，同时也是 user_role.role_id 的关联值。 */
    private Long id;

    /** 角色标识名称，例如管理员或普通用户角色，对应 role_name 列。 */
    private String roleName;
}
