package com.petadoption.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册表单 DTO，只承载注册页面提交的数据及 Bean Validation 校验规则。
 * Lombok 的 @Data 自动生成各表单字段的 getter、setter 等方法。
 */
@Data
public class RegistrationForm {
    /** 注册用户名，提交后由业务层用于创建账号。 */
    // 用户名不能为 null、空字符串或纯空白字符。
    @NotBlank(message = "请输入用户名")
    // 用户名长度须在 3 到 50 个字符之间。
    @Size(min = 3, max = 50, message = "用户名长度应为3到50个字符")
    // 用户名仅允许英文字母、数字和下划线。
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /** 注册密码，保存前所需的加密处理由业务层完成。 */
    // 注册时密码不能为空。
    @NotBlank(message = "请输入密码")
    // 密码长度须在 6 到 72 个字符之间。
    @Size(min = 6, max = 72, message = "密码长度应为6到72个字符")
    private String password;

    /** 第二次输入的密码，用于与 password 进行一致性核对。 */
    // 此处只校验确认密码非空，两次密码是否一致由后续业务校验负责。
    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;

    /** 注册邮箱。 */
    // 邮箱为必填项。
    @NotBlank(message = "请输入邮箱")
    // 按标准邮箱格式校验输入内容。
    @Email(message = "请输入有效的邮箱地址")
    // 邮箱最多允许 100 个字符。
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    /** 联系电话，可不填写。 */
    // 填写联系电话时最多允许 30 个字符。
    @Size(max = 30, message = "联系电话不能超过30个字符")
    private String phone;
}
