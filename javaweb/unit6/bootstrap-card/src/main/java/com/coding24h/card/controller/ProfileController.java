package com.coding24h.card.controller;
import com.coding24h.card.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class ProfileController {
    @GetMapping("/")
    public String getProfile(Model model) {
        // 创建三个User对象
        User user1 = new User(
                "张大为",
                32,
                "zhangdw@example.com",
                "一位经验丰富的Java后端开发者，热衷于探索新技术和构建高性能应用。",
                Arrays.asList("Java", "Spring Boot", "MySQL", "Redis", "Docker")
        );
        User user2 = new User(
                "李晓红",
                28,
                "lixh@example.com",
                "前端工程师，对UI/UX设计有浓厚兴趣，喜欢用代码创造美观且易用的界面。",
                Arrays.asList("JavaScript", "React", "Vue.js", "CSS3", "Figma")
        );
        User user3 = new User(
                "王明",
                45,
                "wangm@example.com",
                "全栈开发者，拥有超过20年的从业经验，是团队中的技术导师和问题解决专家。",
                Arrays.asList("Python", "Django", "Angular", "AWS", "System Design")
        );
        // 将用户列表添加到模型中
        List<User> users = Arrays.asList(user1, user2, user3);
        model.addAttribute("users", users);
        model.addAttribute("now", LocalDateTime.now());
        return "profile";
    }
}