package com.me.book.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {

    /**
     * 用户登录接口
     * @param name 用户名
     * @param password 密码
     * @param session HttpSession
     * @return boolean
     */
    @PostMapping("/login")
    public boolean login(String name, String password, HttpSession session){
        //非空校验
        if (!StringUtils.hasLength(name) || !StringUtils.hasLength(password)){
            return false;
        }

        //校验账号密码
        if(!"admin".equals(name) || !"123456".equals(password)){
            //账号密码错误
            return false;
        }

        // 账号密码正确, 存入session后返回true
        session.setAttribute("userName", name);
        return true;
    }
}
