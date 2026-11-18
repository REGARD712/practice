package com.petadoption.controller;

import com.petadoption.config.AuthenticatedUser;
import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.Pet;
import com.petadoption.exception.BusinessException;
import com.petadoption.service.ApplicationService;
import com.petadoption.service.PetService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 登录用户提交和查看领养申请的控制器。
 * 相关 URL 已在安全配置中声明为必须认证，申请人身份取自当前会话而非表单。
 */
@Controller
@RequiredArgsConstructor
public class ApplicationController {
    /** 提供本人申请查询和申请提交逻辑。 */
    private final ApplicationService applicationService;
    /** 提供申请页面所需的宠物详情。 */
    private final PetService petService;

    /**
     * 展示当前登录用户提交的全部领养申请。
     *
     * @param principal 当前认证用户，其不可变主键用于限定数据归属
     * @param model 向模板传递本人申请列表
     * @return 我的申请模板逻辑名
     */
    @GetMapping("/applications/mine")
    public String mine(@AuthenticationPrincipal AuthenticatedUser principal, Model model) {
        // 仅按会话中的用户主键查询，避免通过请求参数查看他人的申请。
        model.addAttribute("applications",
                applicationService.findMine(principal == null ? null : principal.getId()));
        return "applications/mine";
    }

    /**
     * 打开指定宠物的领养申请表单。
     *
     * @param id 路径中的宠物编号
     * @param model 向模板传递宠物信息和空申请对象
     * @return 领养申请表单模板逻辑名
     */
    @GetMapping("/pets/{id}/apply")
    public String applyForm(@PathVariable Long id, Model model) {
        // 加载宠物供页面展示，同时由服务层处理宠物不存在的情况。
        Pet pet = petService.getById(id);
        model.addAttribute("pet", pet);
        // 提供新的空对象承接申请表单字段。
        // 模型属性不能命名为 application：Thymeleaf 3.1 用该名字暴露 ServletContext 属性表，
        // 会覆盖同名模型属性，导致模板的 th:object 取不到表单对象。
        model.addAttribute("applicationDetail", new AdoptionApplication());
        // 页面层提前提示非 AVAILABLE 状态；最终提交仍由服务层再次校验并并发保护。
        if (!"AVAILABLE".equals(pet.getStatus())) {
            model.addAttribute("error", "该宠物当前不可申请领养");
        }
        return "pets/apply";
    }

    /**
     * 校验并提交领养申请。
     *
     * @param id 被申请宠物编号，由服务层重新绑定到申请记录
     * @param application 页面填写并经过 Bean Validation 校验的申请资料
     * @param bindingResult 保存字段校验和业务错误
     * @param principal 当前认证用户，使用其不可变主键确定申请人身份
     * @param model 失败时重新渲染申请页所需模型
     * @param redirectAttributes 成功后向本人申请列表传递一次性提示
     * @return 失败返回申请表单，成功重定向到本人申请列表
     */
    @PostMapping("/pets/{id}/apply")
    public String apply(@PathVariable Long id,
                        @Valid @ModelAttribute("applicationDetail") AdoptionApplication application,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal AuthenticatedUser principal,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        // 基础字段不合法时不进入业务层，重新加载宠物并保留表单错误。
        if (bindingResult.hasErrors()) {
            model.addAttribute("pet", petService.getById(id));
            return "pets/apply";
        }
        try {
            // 服务层忽略客户端身份和状态字段，初始化 PENDING，并原子抢占宠物申请资格。
            applicationService.submit(id, principal == null ? null : principal.getId(), application);
        } catch (BusinessException ex) {
            // 将不可申请、并发抢占失败或登录用户异常等原因回显在表单上。
            bindingResult.reject("application.failed", ex.getMessage());
            model.addAttribute("pet", petService.getById(id));
            return "pets/apply";
        }
        // 采用提交后重定向，防止刷新页面造成重复申请。
        redirectAttributes.addFlashAttribute("success", "领养申请已提交，请耐心等待审核");
        return "redirect:/applications/mine";
    }
}
