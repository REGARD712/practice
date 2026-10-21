package com.coding24h.imessage.controller;

import com.coding24h.imessage.entity.Message;
import com.coding24h.imessage.service.MessageService;
import com.coding24h.imessage.service.UserService;
import com.coding24h.imessage.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final CategoryService categoryService;

    MessageController(MessageService messageService, UserService userService, CategoryService categoryService) {
        this.messageService = messageService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    // 根路径直接展示消息列表
    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("messages", messageService.getAllWithUserAndCategory());
        return "message/list";
    }

    // 消息列表页（也可通过 /messages 访问）
    @GetMapping("/messages")
    public String listMessages(Model model) {
        return list(model);
    }

    // 新增消息页面
    @GetMapping("/messages/add")
    public String addForm(Model model) {
        model.addAttribute("message", new Message());
        model.addAttribute("users", userService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        return "message/form";
    }

    // 保存新增消息
    @PostMapping("/messages")
    public String save(Message message) {
        messageService.add(message);
        return "redirect:/";
    }

    // 编辑消息页面
    @GetMapping("/messages/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("message", messageService.getById(id));
        model.addAttribute("users", userService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        return "message/form";
    }

    // 保存更新消息
    @PutMapping("/messages/{id}")
    public String update(@PathVariable Long id, Message message) {
        message.setId(id);
        messageService.update(message);
        return "redirect:/";
    }

    // 删除消息
    @DeleteMapping("/messages/{id}")
    public String delete(@PathVariable Long id) {
        messageService.delete(id);
        return "redirect:/";
    }
}