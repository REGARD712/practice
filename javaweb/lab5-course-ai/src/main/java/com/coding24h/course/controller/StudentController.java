package com.coding24h.course.controller;

import com.coding24h.course.entity.User;
import com.coding24h.course.service.CourseService;
import com.coding24h.course.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public StudentController(CourseService courseService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/courses")
    public String browse(@SessionAttribute("currentUser") User student, Model model) {
        var enrolledCourses = courseService.findEnrolledByStudent(student.getId());
        Set<Long> enrolledIds = enrolledCourses.stream().map(course -> course.getId()).collect(Collectors.toSet());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("enrolledIds", enrolledIds);
        return "student/courses";
    }

    @PostMapping("/enroll")
    public String enroll(@RequestParam Long courseId, @SessionAttribute("currentUser") User student,
                         RedirectAttributes redirect) {
        try {
            enrollmentService.enroll(student.getId(), courseId);
            redirect.addFlashAttribute("message", "选课成功");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/courses";
    }

    @GetMapping("/my-courses")
    public String myCourses(@SessionAttribute("currentUser") User student, Model model) {
        model.addAttribute("enrollments", enrollmentService.findByStudent(student.getId()));
        return "student/my-courses";
    }

    @PostMapping("/my-courses/{courseId}/withdraw")
    public String withdraw(@PathVariable Long courseId, @SessionAttribute("currentUser") User student,
                           RedirectAttributes redirect) {
        try {
            enrollmentService.withdraw(student.getId(), courseId);
            redirect.addFlashAttribute("message", "已退选课程");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/student/my-courses";
    }
}
