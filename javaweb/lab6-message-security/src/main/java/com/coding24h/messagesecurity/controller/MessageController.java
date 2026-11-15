package com.coding24h.messagesecurity.controller;

import com.coding24h.messagesecurity.service.MessageService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/message")
public class MessageController {
    private final MessageService messageService;

    MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public String send(@RequestParam String content, Authentication auth) {
        if (content != null && !content.trim().isEmpty()) {
            messageService.sendMessage(content.trim(), auth);
        }
        return "redirect:/messages";
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Authentication auth) {
        messageService.deleteMessage(id, auth);
        return "redirect:/messages";
    }
}