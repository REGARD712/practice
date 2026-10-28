package com.coding24h.course.controller;

import com.coding24h.course.entity.Course;
import com.coding24h.course.entity.User;
import com.coding24h.course.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/courses")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public String list(@SessionAttribute("currentUser") User teacher, Model model) {
        model.addAttribute("courses", courseService.findByTeacher(teacher.getId()));
        return "teacher/courses";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("formAction", "/teacher/courses");
        return "teacher/course-form";
    }

    @PostMapping
    public String create(@ModelAttribute Course course, @RequestParam(required = false) MultipartFile cover,
                         @SessionAttribute("currentUser") User teacher, RedirectAttributes redirect) {
        try {
            courseService.create(course, cover, teacher.getId());
            redirect.addFlashAttribute("message", "课程已发布");
            return "redirect:/teacher/courses";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/teacher/courses/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @SessionAttribute("currentUser") User teacher,
                           Model model) {
        Course course = courseService.findById(id);
        if (!course.getTeacherId().equals(teacher.getId())) {
            throw new IllegalArgumentException("无权编辑其他教师的课程");
        }
        model.addAttribute("course", course);
        model.addAttribute("formAction", "/teacher/courses/" + id);
        return "teacher/course-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Course course,
                         @RequestParam(required = false) MultipartFile cover,
                         @SessionAttribute("currentUser") User teacher, RedirectAttributes redirect) {
        course.setId(id);
        try {
            courseService.update(course, cover, teacher.getId(), false);
            redirect.addFlashAttribute("message", "课程已更新");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/teacher/courses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @SessionAttribute("currentUser") User teacher,
                         RedirectAttributes redirect) {
        try {
            courseService.delete(id, teacher.getId(), false);
            redirect.addFlashAttribute("message", "课程已删除");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/teacher/courses";
    }
}
