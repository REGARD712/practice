package com.coding24h.securityrbac.controller;

import com.coding24h.securityrbac.service.DocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    private final DocumentService documentService;

    UserController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/docs")
    public String viewDocs(Model model) {
        String content = documentService.viewDocument(1001L);
        model.addAttribute("docContent", content);
        return "user/docs";
    }

    @GetMapping("/editDoc")
    public String editDoc(Model model) {
        String result = documentService.editDocument(1001L);
        model.addAttribute("editResult", result);
        return "user/editResult";
    }
}
