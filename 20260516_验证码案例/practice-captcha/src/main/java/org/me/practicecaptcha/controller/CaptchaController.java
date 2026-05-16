package org.me.practicecaptcha.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.me.practicecaptcha.model.CaptchaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;

@RequestMapping("/captcha")
@RestController
public class CaptchaController {
    @Autowired
    private CaptchaProperties captchaProperties;
    // 过期时间
    private static final Long VALUE_TIME = 1000L * 60 * 3;

    @RequestMapping("/getCaptcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) {
        response.setContentType("image/jpeg");
        try {
            // 生成验证码
            ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(captchaProperties.getWidth(), captchaProperties.getHeight());;
            // 将验证码和生成时间存入session中，用于后续验证
            session.setAttribute(captchaProperties.getSession().getCode(), captcha.getCode());;
            session.setAttribute(captchaProperties.getSession().getDate(), new Date());
            // 打印日志
            System.out.println(captcha.getCode());
            // 将验证码图片输出到响应流中
            captcha.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/checkCaptcha")
    public Boolean checkCaptcha(String captcha, HttpSession session) {
        if (!StringUtils.hasLength(captcha)) {
            return false;
        }

        // 先从session中获取验证码以及对应的生成时间
        String code = session.getAttribute(captchaProperties.getSession().getCode()).toString();
        Date date = (Date)session.getAttribute(captchaProperties.getSession().getDate());
        // 判断是否有效
        if (code.equalsIgnoreCase(captcha) && date != null && System.currentTimeMillis() - date.getTime() < VALUE_TIME) {
            return true;
        }
        return false;
    }
}
