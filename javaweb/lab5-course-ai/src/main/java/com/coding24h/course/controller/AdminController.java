package com.coding24h.course.controller;

import com.coding24h.course.entity.Course;
import com.coding24h.course.entity.User;
import com.coding24h.course.mapper.UserMapper;
import com.coding24h.course.service.CourseService;
import com.coding24h.course.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final CourseService courseService;

    public AdminController(UserService userService, UserMapper userMapper, CourseService courseService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.courseService = courseService;
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("formAction", "/admin/users");
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirect) {
        try {
            userService.create(user);
            redirect.addFlashAttribute("message", "用户已创建");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/users/new";
        }
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPassword("");
        model.addAttribute("user", user);
        model.addAttribute("formAction", "/admin/users/" + id);
        return "admin/user-form";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, RedirectAttributes redirect) {
        user.setId(id);
        try {
            userService.update(user);
            redirect.addFlashAttribute("message", "用户信息已更新");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, @SessionAttribute("currentUser") User admin,
                             RedirectAttributes redirect) {
        try {
            userService.delete(id, admin.getId());
            redirect.addFlashAttribute("message", "用户已删除");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "admin/courses";
    }

    @GetMapping("/courses/new")
    public String newCourse(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("teachers", userMapper.findByRole(User.Role.TEACHER));
        model.addAttribute("formAction", "/admin/courses");
        return "admin/course-form";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course course, @RequestParam Long teacherId,
                               @RequestParam(required = false) MultipartFile cover,
                               RedirectAttributes redirect) {
        try {
            courseService.create(course, cover, teacherId);
            redirect.addFlashAttribute("message", "课程已创建");
            return "redirect:/admin/courses";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/courses/new";
        }
    }

    @GetMapping("/courses/{id}/edit")
    public String editCourse(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("teachers", userMapper.findByRole(User.Role.TEACHER));
        model.addAttribute("formAction", "/admin/courses/" + id);
        return "admin/course-form";
    }

    @PostMapping("/courses/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course,
                               @RequestParam(required = false) MultipartFile cover,
                               @SessionAttribute("currentUser") User admin, RedirectAttributes redirect) {
        course.setId(id);
        try {
            courseService.update(course, cover, admin.getId(), true);
            redirect.addFlashAttribute("message", "课程已更新");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id, @SessionAttribute("currentUser") User admin,
                               RedirectAttributes redirect) {
        try {
            courseService.delete(id, admin.getId(), true);
            redirect.addFlashAttribute("message", "课程已删除");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/courses";
    }
}
