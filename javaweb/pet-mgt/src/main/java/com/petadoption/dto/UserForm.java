package com.petadoption.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 后台用户新增、编辑表单 DTO，承载账号资料及待分配的角色 ID。
 * Lombok 的 @Data 自动生成表单字段的访问器和常用对象方法。
 */
@Data
public class UserForm {
    /** 用户主键；新增时通常为空，编辑时用于定位待更新用户。 */
    private Long id;

    /** 登录用户名。 */
    // 用户名不能为 null、空字符串或纯空白字符。
    @NotBlank(message = "请输入用户名")
    // 用户名长度须在 3 到 50 个字符之间。
    @Size(min = 3, max = 50, message = "用户名长度应为3到50个字符")
    // 用户名仅允许英文字母、数字和下划线。
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 可选的新密码，最多 72 个字符。
     * 编辑用户时留空表示不修改原密码，UserMapper 的动态 UPDATE 会据此省略 password 赋值。
     */
    @Size(max = 72, message = "密码不能超过72个字符")
    private String password;

    /** 用户邮箱。 */
    // 邮箱为必填项。
    @NotBlank(message = "请输入邮箱")
    // 校验输入内容是否符合邮箱格式。
    @Email(message = "请输入有效的邮箱地址")
    // 邮箱最多允许 100 个字符。
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    /** 联系电话，可不填写。 */
    // 填写联系电话时最多允许 30 个字符。
    @Size(max = 30, message = "联系电话不能超过30个字符")
    private String phone;

    /** 账号是否启用，表单对象创建时默认设为 true。 */
    private Boolean enabled = true;

    /** 待分配给用户的角色主键集合，对应 user_role 表中的 role_id。 */
    // 集合不能为 null 或空集合，即每个用户至少要分配一个角色。
    @NotEmpty(message = "请至少分配一个角色")
    private List<Long> roleIds;
}
