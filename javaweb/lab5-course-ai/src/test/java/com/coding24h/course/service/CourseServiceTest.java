package com.coding24h.course.service;

import com.coding24h.course.entity.Course;
import com.coding24h.course.entity.User;
import com.coding24h.course.mapper.CourseMapper;
import com.coding24h.course.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CourseServiceTest {
    @TempDir
    Path uploadDirectory;

    @Test
    void createsCourseAndStoresCoverWithUniqueName() throws Exception {
        CourseMapper courseMapper = mock(CourseMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        User teacher = new User();
        teacher.setId(7L);
        teacher.setRole(User.Role.TEACHER);
        when(userMapper.findById(7L)).thenReturn(teacher);

        CourseService service = new CourseService(courseMapper, userMapper, uploadDirectory.toString());
        Course course = new Course();
        course.setName("Java 入门");
        course.setPrice(new BigDecimal("19.90"));
        MockMultipartFile cover = new MockMultipartFile(
                "cover", "cover.png", "image/png", pngImage());

        service.create(course, cover, 7L);

        assertEquals(7L, course.getTeacherId());
        assertTrue(course.getCoverUrl().matches("/uploads/[0-9a-f-]{36}\\.png"));
        assertTrue(Files.exists(uploadDirectory.resolve(course.getCoverUrl().substring("/uploads/".length()))));
        verify(courseMapper).insert(any(Course.class));
    }

    @Test
    void rejectsNonImageCover() {
        CourseMapper courseMapper = mock(CourseMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        User teacher = new User();
        teacher.setRole(User.Role.TEACHER);
        when(userMapper.findById(7L)).thenReturn(teacher);
        CourseService service = new CourseService(courseMapper, userMapper, uploadDirectory.toString());

        Course course = new Course();
        course.setName("Java 入门");
        course.setPrice(BigDecimal.ZERO);
        MockMultipartFile cover = new MockMultipartFile("cover", "cover.jpg", "image/jpeg", "not an image".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.create(course, cover, 7L));
        verify(courseMapper, never()).insert(any(Course.class));
    }

    private byte[] pngImage() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
