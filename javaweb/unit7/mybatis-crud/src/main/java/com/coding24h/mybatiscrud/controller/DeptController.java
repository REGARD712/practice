package com.coding24h.mybatiscrud.controller;

import com.coding24h.mybatiscrud.entity.Dept;
import com.coding24h.mybatiscrud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DeptController {
    @Autowired
    private DeptService deptService;

    // 根路径访问，重定向到部门列表
    @GetMapping("/")
    public String root() {
        return "redirect:/depts";
    }

    // 列表页
    @GetMapping("/depts")
    public String list(Model model) {
        model.addAttribute("depts", deptService.getAll());
        return "list";
    }

    // 新增页面
    @GetMapping("/depts/add")
    public String addForm(Model model) {
        model.addAttribute("dept", new Dept());
        return "form";
    }

    // 保存新增
    @PostMapping("/depts")
    public String save(Dept dept) {
        deptService.add(dept);
        return "redirect:/depts";
    }

    // 编辑页面
    @GetMapping("/depts/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("dept", deptService.getById(id));
        return "form";
    }

    // 保存更新
    @PutMapping("/depts/{id}")
    public String update(@PathVariable Long id, Dept dept) {
        dept.setId(id);
        deptService.update(dept);
        return "redirect:/depts";
    }

    // 删除
    @DeleteMapping("/depts/{id}")
    public String delete(@PathVariable Long id) {
        deptService.delete(id);
        return "redirect:/depts";
    }
}
