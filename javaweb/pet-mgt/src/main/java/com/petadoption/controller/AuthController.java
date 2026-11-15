package com.petadoption.controller;

import com.petadoption.dto.RegistrationForm;
import com.petadoption.exception.BusinessException;
import com.petadoption.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 登录页和自助注册控制器。
 * 登录认证本身由 Spring Security 处理，本类只展示页面并完成普通用户注册。
 */
@Controller
@RequiredArgsConstructor
public class AuthController {
    /** 提供注册、密码加密、唯一性检查和默认角色分配。 */
    private final UserService userService;

    /**
     * 展示自定义登录页面。
     *
     * @return 登录模板逻辑名
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 打开展示空白注册表单。
     *
     * @param model 向模板传递注册表单对象
     * @return 注册模板逻辑名
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        // 创建绑定对象，承接用户名、密码、联系方式等注册字段。
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    /**
     * 校验注册信息并创建普通用户。
     *
     * @param registrationForm 页面绑定并经过 Bean Validation 校验的注册数据
     * @param bindingResult 保存字段校验错误和业务错误
     * @param redirectAttributes 注册成功后向登录页传递一次性提示
     * @return 失败返回注册页，成功重定向到登录页
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationForm registrationForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        // 在字段级校验之外比较两次密码，不一致时把错误精确绑定到确认密码字段。
        if (registrationForm.getPassword() != null
                && !registrationForm.getPassword().equals(registrationForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "两次输入的密码不一致");
        }
        // 任一校验失败均直接返回，保留合法输入供用户修改。
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            // 服务层规范化账号资料、加密密码、检查唯一性并只授予 ROLE_USER。
            userService.register(registrationForm);
        } catch (BusinessException ex) {
            // 用户名邮箱冲突或默认角色缺失等业务问题作为表单级错误展示。
            bindingResult.reject("registration.failed", ex.getMessage());
            return "register";
        }
        // 注册与登录分离，成功后由用户主动登录以建立安全会话。
        redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
        return "redirect:/login?registered";
    }
}
