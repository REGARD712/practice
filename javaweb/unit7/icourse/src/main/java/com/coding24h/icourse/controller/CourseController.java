package com.coding24h.icourse.controller;

import com.coding24h.icourse.entity.Course;
import com.coding24h.icourse.mapper.UserMapper;
import com.coding24h.icourse.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserMapper userMapper;

    public CourseController(CourseService courseService, UserMapper userMapper) {
        this.courseService = courseService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "course/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("teachers", userMapper.findAllTeachers());
        model.addAttribute("editMode", false);
        return "course/form";
    }

    @PostMapping
    public String save(@ModelAttribute Course course,
                       @RequestParam(name = "cover", required = false) MultipartFile cover) throws IOException {
        courseService.add(course, cover);
        return "redirect:/courses";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        if (course == null) {
            return "redirect:/courses";
        }
        model.addAttribute("course", course);
        model.addAttribute("teachers", userMapper.findAllTeachers());
        model.addAttribute("editMode", true);
        return "course/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Course course,
                         @RequestParam(name = "cover", required = false) MultipartFile cover) throws IOException {
        courseService.update(id, course, cover);
        return "redirect:/courses";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        courseService.delete(id);
        return "redirect:/courses";
    }
}
