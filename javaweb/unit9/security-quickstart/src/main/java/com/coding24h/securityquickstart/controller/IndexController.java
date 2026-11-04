package com.coding24h.securityquickstart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {
    @GetMapping("/")
    public String home() {
        return "home"; // 返回 templates/home.html
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello, Security!";
    }
}
