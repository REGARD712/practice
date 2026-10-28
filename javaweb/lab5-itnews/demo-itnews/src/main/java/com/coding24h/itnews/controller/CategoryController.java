package com.coding24h.itnews.controller;

import com.coding24h.itnews.entity.Category;
import com.coding24h.itnews.service.CategoryService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    /** 依赖通过构造器注入，保证不可变并在启动阶段暴露装配问题 */
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/category_list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category_form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category) {
        // 名称为空会在数据库层触发非空约束并返回 500，这里提前拦截并回到表单页
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return category.getId() == null
                    ? "redirect:/admin/categories/add"
                    : "redirect:/admin/categories/edit/" + category.getId();
        }
        category.setName(category.getName().trim());
        if (category.getId() == null) {
            categoryService.add(category);
        } else {
            categoryService.update(category);
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        // 记录不存在时必须回到列表页，否则 Thymeleaf 绑定 null 对象会抛异常
        if (category == null) {
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", category);
        return "admin/category_form";
    }

    /**
     * 删除使用 POST：GET 请求可能被浏览器预取、爬虫或第三方页面（如 img src）触发，导致数据被误删。
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}
