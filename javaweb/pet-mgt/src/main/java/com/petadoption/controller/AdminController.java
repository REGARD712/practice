package com.petadoption.controller;

import com.petadoption.config.AuthenticatedUser;
import com.petadoption.dto.UserForm;
import com.petadoption.exception.BusinessException;
import com.petadoption.service.ApplicationService;
import com.petadoption.service.PetService;
import com.petadoption.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * 管理后台首页与用户管理控制器。
 * /admin 下的全部路由仅允许管理员访问，写操作通过 POST 提交。
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    /** 提供宠物数量及各状态统计。 */
    private final PetService petService;
    /** 提供用户查询、创建、修改、删除和角色数据。 */
    private final UserService userService;
    /** 提供后台最近领养申请列表。 */
    private final ApplicationService applicationService;

    /**
     * 展示管理后台仪表盘。
     *
     * @param model 向模板传递各类统计数据和近期申请
     * @return 后台仪表盘模板逻辑名
     */
    @GetMapping
    public String dashboard(Model model) {
        // 统计全部宠物数量。
        model.addAttribute("petCount", petService.count());
        // 统计可申请领养的宠物数量。
        model.addAttribute("availableCount", petService.countByStatus("AVAILABLE"));
        // 统计已有待审核申请、暂不可重复申请的宠物数量。
        model.addAttribute("pendingCount", petService.countByStatus("PENDING"));
        // 统计已被领养的宠物数量。
        model.addAttribute("adoptedCount", petService.countByStatus("ADOPTED"));
        // 统计系统用户数量。
        model.addAttribute("userCount", userService.count());
        // 查询近期申请，便于管理员快速进入审核工作。
        model.addAttribute("recentApplications", applicationService.findRecent());
        return "admin/dashboard";
    }

    /**
     * 展示全部系统用户。
     *
     * @param model 向用户列表传递用户及角色信息
     * @return 用户管理列表模板逻辑名
     */
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    /**
     * 打开新建用户表单，并预置普通用户角色。
     *
     * @param model 向表单传递空用户和可选角色
     * @return 用户表单模板逻辑名
     */
    @GetMapping("/users/new")
    public String newUser(Model model) {
        // newForm 默认选中 ROLE_USER，减少误授予管理员权限的可能。
        model.addAttribute("userForm", userService.newForm());
        // 提供全部合法角色供管理员选择。
        model.addAttribute("roles", userService.findAllRoles());
        return "admin/users/form";
    }

    /**
     * 校验并创建新用户。
     *
     * @param userForm 页面绑定并经过 Bean Validation 校验的用户数据
     * @param bindingResult 保存字段校验及业务错误
     * @param model 校验失败时重新渲染表单所需模型
     * @param redirectAttributes 成功后向列表页传递一次性提示
     * @return 失败返回表单，成功重定向到用户列表
     */
    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute("userForm") UserForm userForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        // 基础字段校验失败时不进入服务层，直接返回并保留用户输入。
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", userService.findAllRoles());
            return "admin/users/form";
        }
        try {
            // 服务层继续执行密码规则、角色有效性及用户名邮箱唯一性检查。
            userService.create(userForm);
        } catch (BusinessException ex) {
            // 将唯一性等业务错误登记为表单级错误，避免丢失其他已填写内容。
            bindingResult.reject("user.save.failed", ex.getMessage());
            model.addAttribute("roles", userService.findAllRoles());
            return "admin/users/form";
        }
        // 使用重定向避免刷新页面导致重复创建。
        redirectAttributes.addFlashAttribute("success", "用户创建成功");
        return "redirect:/admin/users";
    }

    /**
     * 打开指定用户的编辑表单。
     *
     * @param id 待编辑用户编号
     * @param model 向模板传递用户表单数据和全部角色
     * @return 用户编辑表单模板逻辑名
     */
    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        // 服务层加载现有资料及当前角色，用户不存在时抛出资源异常。
        model.addAttribute("userForm", userService.getForm(id));
        model.addAttribute("roles", userService.findAllRoles());
        return "admin/users/form";
    }

    /**
     * 校验并更新指定用户。
     *
     * @param id 路径中指定的用户编号，是本次更新的权威标识
     * @param userForm 页面绑定并校验后的用户资料
     * @param bindingResult 保存字段及业务校验错误
     * @param principal 当前登录管理员，服务层据此识别“编辑自己”并施加自保护
     * @param model 失败时重新渲染表单所需模型
     * @param redirectAttributes 成功后传递一次性提示
     * @return 失败返回表单，成功重定向到用户列表
     */
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("userForm") UserForm userForm,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal AuthenticatedUser principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        // 以路径编号覆盖表单编号，防止篡改隐藏字段后更新其他用户。
        userForm.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", userService.findAllRoles());
            return "admin/users/form";
        }
        try {
            // 服务层在事务内执行自保护、“至少保留一名管理员”校验，并更新资料与角色关联。
            userService.update(userForm, principal == null ? null : principal.getId());
        } catch (BusinessException ex) {
            bindingResult.reject("user.save.failed", ex.getMessage());
            model.addAttribute("roles", userService.findAllRoles());
            return "admin/users/form";
        }
        redirectAttributes.addFlashAttribute("success", "用户信息已更新");
        return "redirect:/admin/users";
    }

    /**
     * 删除指定用户，同时阻止管理员删除当前登录账号。
     *
     * @param id 待删除用户编号
     * @param principal 当前已认证管理员信息
     * @param redirectAttributes 向列表页传递操作结果
     * @return 始终重定向到用户列表
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            // 比较登录名与目标账号，避免删除自己后造成会话及管理操作异常。
            if (principal != null && principal.getName().equals(userService.getById(id).getUsername())) {
                throw new BusinessException("不能删除当前登录账号");
            }
            // 服务层还会拒绝删除已有领养申请的用户，并清理角色关联。
            userService.delete(id);
            redirectAttributes.addFlashAttribute("success", "用户已删除");
        } catch (BusinessException ex) {
            // 将自删保护或关联数据限制等业务原因反馈到列表页。
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
