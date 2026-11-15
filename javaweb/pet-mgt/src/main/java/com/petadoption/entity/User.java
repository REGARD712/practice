package com.petadoption.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体，对应数据库 user 表。
 * Lombok 的 @Data 自动生成 getter、setter 等常用方法。
 */
@Data
public class User {
    /** 用户主键，对应 id 列。 */
    private Long id;

    /** 登录用户名，对应 username 列。 */
    private String username;

    /** 密码字段，对应 password 列；加密与校验逻辑由业务层负责。 */
    private String password;

    /** 电子邮箱，对应 email 列。 */
    private String email;

    /** 联系电话，对应 phone 列，允许不填写。 */
    private String phone;

    /** 头像图片路径，对应 avatar 列。 */
    private String avatar;

    /** 账号是否启用，对应 enabled 列；false 表示账号被停用。 */
    private Boolean enabled;

    /** 账号创建时间，对应 created_at 列。 */
    private LocalDateTime createdAt;

    /**
     * 用户拥有的角色集合，不是 user 表中的直接字段。
     * UserMapper 通过当前用户 id 查询 user_role 与 role 表后填充该集合。
     */
    private List<Role> roles;
}
