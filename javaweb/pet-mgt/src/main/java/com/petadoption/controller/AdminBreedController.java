package com.petadoption.controller;

import com.petadoption.dto.BreedForm;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.service.BreedService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 后台品种管理控制器，提供品种列表、新增、改名和删除入口。
 * 所有写操作均通过 POST 表单提交，并受 Spring Security CSRF 保护。
 */
@Controller
@RequestMapping("/admin/breeds")
public class AdminBreedController {
    /** 品种业务服务。 */
    private final BreedService breedService;

    /**
     * 通过构造器注入品种服务。
     *
     * @param breedService 品种业务服务
     */
    public AdminBreedController(BreedService breedService) {
        this.breedService = breedService;
    }

    /**
     * 品种列表页，展示品种名称、物种和关联宠物数量。
     *
     * @param model 视图模型
     * @return 品种管理模板
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("breeds", breedService.findAllWithPetCount());
        // 顶部新增表单需要一个空对象承接输入；校验失败回显时保留已绑定的对象。
        if (!model.containsAttribute("breedForm")) {
            model.addAttribute("breedForm", new BreedForm());
        }
        return "admin/breeds/list";
    }

    /**
     * 新增品种；表单校验失败时回显列表并保留输入。
     *
     * @param breedForm 新增品种表单
     * @param bindingResult 校验结果
     * @param model 视图模型
     * @param redirectAttributes 成功或业务失败的提示
     * @return 成功时重定向回列表，校验失败时返回列表模板
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("breedForm") BreedForm breedForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("breeds", breedService.findAllWithPetCount());
            return "admin/breeds/list";
        }
        try {
            breedService.create(breedForm);
            redirectAttributes.addFlashAttribute("success", "品种「" + breedForm.getName().trim() + "」已新增");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/breeds";
    }

    /**
     * 修改品种名称。
     *
     * @param id 品种编号
     * @param name 新名称
     * @param redirectAttributes 操作结果提示
     * @return 重定向回列表
     */
    @PostMapping("/{id}")
    public String rename(@PathVariable Long id,
                         @RequestParam("name") String name,
                         RedirectAttributes redirectAttributes) {
        try {
            breedService.rename(id, name);
            redirectAttributes.addFlashAttribute("success", "品种名称已更新");
        } catch (BusinessException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/breeds";
    }

    /**
     * 删除品种；仍被宠物引用时由服务层拒绝并返回原因。
     *
     * @param id 品种编号
     * @param redirectAttributes 操作结果提示
     * @return 重定向回列表
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            breedService.delete(id);
            redirectAttributes.addFlashAttribute("success", "品种已删除");
        } catch (BusinessException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/breeds";
    }
}
