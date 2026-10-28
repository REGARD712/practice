package com.coding24h.course.service;

import com.coding24h.course.entity.Course;
import com.coding24h.course.entity.User;
import com.coding24h.course.mapper.CourseMapper;
import com.coding24h.course.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CourseService {
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final Path uploadDirectory;

    public CourseService(CourseMapper courseMapper, UserMapper userMapper,
                         @Value("${file.upload-dir:uploads}") String uploadDirectory) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public Course create(Course course, MultipartFile cover, Long teacherId) {
        validate(course);
        requireTeacher(teacherId);
        course.setTeacherId(teacherId);
        course.setCoverUrl(saveCover(cover, null));
        courseMapper.insert(course);
        return course;
    }

    @Transactional
    public Course update(Course course, MultipartFile cover, Long actingUserId, boolean administrator) {
        Course existing = requireCourse(course.getId());
        ensureCanManage(existing, actingUserId, administrator);
        validate(course);
        Long teacherId = administrator && course.getTeacherId() != null
                ? course.getTeacherId() : existing.getTeacherId();
        requireTeacher(teacherId);
        course.setTeacherId(teacherId);
        String oldCoverUrl = existing.getCoverUrl();
        course.setCoverUrl(saveCover(cover, oldCoverUrl));
        courseMapper.update(course);
        if (!Objects.equals(oldCoverUrl, course.getCoverUrl())) {
            deleteCover(oldCoverUrl);
        }
        return course;
    }

    @Transactional
    public void delete(Long courseId, Long actingUserId, boolean administrator) {
        Course existing = requireCourse(courseId);
        ensureCanManage(existing, actingUserId, administrator);
        courseMapper.deleteById(courseId);
        deleteCover(existing.getCoverUrl());
    }

    public List<Course> findAll() {
        return courseMapper.findAll();
    }

    public List<Course> findByTeacher(Long teacherId) {
        return courseMapper.findByTeacherId(teacherId);
    }

    public List<Course> findEnrolledByStudent(Long studentId) {
        return courseMapper.findByStudentId(studentId);
    }

    public Course findById(Long courseId) {
        return requireCourse(courseId);
    }

    private Course requireCourse(Long id) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        return course;
    }

    private void requireTeacher(Long id) {
        User teacher = userMapper.findById(id);
        if (teacher == null || teacher.getRole() != User.Role.TEACHER) {
            throw new IllegalArgumentException("课程教师不存在或角色无效");
        }
    }

    private void ensureCanManage(Course course, Long actingUserId, boolean administrator) {
        if (!administrator && !course.getTeacherId().equals(actingUserId)) {
            throw new IllegalArgumentException("无权管理其他教师的课程");
        }
    }

    private void validate(Course course) {
        if (!StringUtils.hasText(course.getName()) || course.getName().length() > 100) {
            throw new IllegalArgumentException("课程名称不能为空且不能超过100个字符");
        }
        if (course.getPrice() == null || course.getPrice().signum() < 0
                || course.getPrice().scale() > 2 || course.getPrice().precision() > 10) {
            throw new IllegalArgumentException("课程价格必须为非负数，最多两位小数");
        }
    }

    private String saveCover(MultipartFile cover, String currentCoverUrl) {
        if (cover == null || cover.isEmpty()) {
            return currentCoverUrl;
        }
        try {
            Files.createDirectories(uploadDirectory);
            BufferedImage image = ImageIO.read(cover.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("封面必须是有效的 PNG、JPEG 或 GIF 图片");
            }
            String format = detectFormat(cover);
            String filename = UUID.randomUUID() + "." + format;
            Path destination = uploadDirectory.resolve(filename).normalize();
            if (!destination.getParent().equals(uploadDirectory)) {
                throw new IllegalArgumentException("无效的封面文件名");
            }
            Files.copy(cover.getInputStream(), destination);
            return "/uploads/" + filename;
        } catch (IOException exception) {
            throw new IllegalStateException("保存课程封面失败", exception);
        }
    }

    private String detectFormat(MultipartFile cover) {
        String extension = StringUtils.getFilenameExtension(cover.getOriginalFilename());
        if (extension == null) {
            throw new IllegalArgumentException("封面文件必须使用 PNG、JPEG 或 GIF 格式");
        }
        String normalized = extension.toLowerCase();
        if (!List.of("png", "jpg", "jpeg", "gif").contains(normalized)) {
            throw new IllegalArgumentException("封面文件必须使用 PNG、JPEG 或 GIF 格式");
        }
        return normalized.equals("jpeg") ? "jpg" : normalized;
    }

    private void deleteCover(String coverUrl) {
        if (!StringUtils.hasText(coverUrl) || !coverUrl.startsWith("/uploads/")) {
            return;
        }
        Path file = uploadDirectory.resolve(coverUrl.substring("/uploads/".length())).normalize();
        if (file.getParent().equals(uploadDirectory)) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException exception) {
                throw new IllegalStateException("删除课程封面失败", exception);
            }
        }
    }
}
