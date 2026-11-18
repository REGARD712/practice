package com.petadoption.controller;

import com.petadoption.exception.BusinessException;
import com.petadoption.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * 管理员审核领养申请的控制器。
 * 所有路由位于 /admin 下，由安全配置限制为 ROLE_ADMIN 角色访问。
 */
@Controller
@RequestMapping("/admin/applications")
@RequiredArgsConstructor
public class AdminApplicationController {
    /** 封装申请查询及审核状态流转逻辑。 */
    private final ApplicationService applicationService;

    /**
     * 按状态筛选并展示全部领养申请。
     *
     * @param status 可选申请状态，如 PENDING、APPROVED 或 REJECTED
     * @param model 向管理列表传递申请数据和筛选条件
     * @return 申请管理列表模板逻辑名
     */
    @GetMapping
    public String applications(@RequestParam(required = false) String status, Model model) {
        // 去除首尾空白并统一为大写，空白筛选值按“不限制状态”处理。
        String normalizedStatus = StringUtils.hasText(status) ? status.trim().toUpperCase(Locale.ROOT) : null;
        // 查询满足状态条件的申请列表。
        model.addAttribute("applications", applicationService.findAll(normalizedStatus));
        // 回显筛选值，保证页面筛选框与当前结果一致。
        model.addAttribute("status", normalizedStatus);
        return "admin/applications/list";
    }

    /**
     * 打开单条申请的审核页面。
     *
     * @param id 待审核申请编号
     * @param model 向审核表单传递申请、宠物及申请人关联信息
     * @return 审核表单模板逻辑名
     */
    @GetMapping("/{id}/review")
    public String reviewForm(@PathVariable Long id, Model model) {
        // 模型属性不能命名为 application：Thymeleaf 3.1 的 WebEngineContext 用该名字暴露
        // ServletContext 属性表，会覆盖同名模型属性，导致模板读到空值。
        model.addAttribute("applicationDetail", applicationService.getById(id));
        return "admin/applications/review";
    }

    /**
     * 提交审核结果，将申请更新为通过或拒绝，并由服务层同步宠物状态。
     *
     * @param id 待审核申请编号
     * @param status 目标审核状态，仅 APPROVED 或 REJECTED 有效
     * @param reviewComment 可选审核意见
     * @param redirectAttributes 跨重定向传递一次性成功或失败提示
     * @return 成功后返回申请列表，业务失败后返回原审核页
     */
    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String reviewComment,
                         RedirectAttributes redirectAttributes) {
        // 统一状态大小写；真正的状态白名单、重复审核和并发检查由服务层完成。
        String normalizedStatus = status == null ? null : status.trim().toUpperCase(Locale.ROOT);
        try {
            // 在单个事务中完成申请审核和宠物状态更新，避免两者不一致。
            applicationService.review(id, normalizedStatus, reviewComment);
            // 通过 flash 属性在重定向后的列表页展示一次成功提示。
            redirectAttributes.addFlashAttribute("success", "领养申请审核完成");
            return "redirect:/admin/applications";
        } catch (BusinessException ex) {
            // 仅将可预期的业务错误反馈给管理员，并保留到审核页重定向后的提示中。
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/applications/" + id + "/review";
        }
    }
}
