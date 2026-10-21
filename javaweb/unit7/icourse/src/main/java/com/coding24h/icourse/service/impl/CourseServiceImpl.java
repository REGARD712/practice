package com.coding24h.icourse.service.impl;

import com.coding24h.icourse.entity.Course;
import com.coding24h.icourse.mapper.CourseMapper;
import com.coding24h.icourse.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Path UPLOAD_DIRECTORY = Paths.get("C:\\upload");
    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Override
    public List<Course> findAll() {
        return courseMapper.findAll();
    }

    @Override
    public Course findById(Long id) {
        return courseMapper.findById(id);
    }

    @Override
    public Course add(Course course, MultipartFile cover) throws IOException {
        if (cover != null && !cover.isEmpty()) {
            course.setCoverUrl(saveCover(cover));
        } else {
            course.setCoverUrl(generateCover(course.getName()));
        }
        courseMapper.insert(course);
        return course;
    }

    @Override
    public boolean update(Long id, Course course, MultipartFile cover) throws IOException {
        Course existingCourse = courseMapper.findById(id);
        if (existingCourse == null) {
            return false;
        }

        course.setId(id);
        String currentCover = existingCourse.getCoverUrl();
        course.setCoverUrl(currentCover == null || currentCover.isBlank()
                ? generateCover(course.getName())
                : currentCover);
        if (cover != null && !cover.isEmpty()) {
            course.setCoverUrl(saveCover(cover));
        }
        return courseMapper.update(course) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return courseMapper.deleteById(id) > 0;
    }

    private String generateCover(String courseName) throws IOException {
        String title = courseName == null || courseName.isBlank() ? "未命名课程" : courseName.trim();
        String safeTitle = escapeXml(title);
        List<String> titleLines = splitTitle(title);
        String palette = switch (Math.floorMod(title.hashCode(), 4)) {
            case 0 -> "#f3a078";
            case 1 -> "#59bdb0";
            case 2 -> "#9278d5";
            default -> "#e6ad55";
        };

        StringBuilder titleMarkup = new StringBuilder();
        int firstLineY = titleLines.size() == 1 ? 270 : 257;
        for (int index = 0; index < titleLines.size(); index++) {
            titleMarkup.append("<text x=\"320\" y=\"")
                    .append(firstLineY + index * 36)
                    .append("\" fill=\"#fffdf6\" font-family=\"Microsoft YaHei, Noto Sans CJK SC, sans-serif\"")
                    .append(" font-size=\"28\" font-weight=\"700\" text-anchor=\"middle\" paint-order=\"stroke\"")
                    .append(" stroke=\"#")
                    .append(palette.substring(1))
                    .append("\" stroke-width=\"2\">")
                    .append(escapeXml(titleLines.get(index)))
                    .append("</text>");
        }

        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="640" height="400" viewBox="0 0 640 400" role="img" aria-labelledby="title description">
                  <title id="title">%s课程封面</title>
                  <desc id="description">根据课程名称自动生成的彩色网课封面。</desc>
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0" stop-color="#fff0d9"/><stop offset=".55" stop-color="%s"/>
                      <stop offset="1" stop-color="#766fc1"/>
                    </linearGradient>
                    <linearGradient id="robot" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0" stop-color="#fffef5"/><stop offset="1" stop-color="#e2fff2"/>
                    </linearGradient>
                    <filter id="shadow" x="-30%%" y="-30%%" width="160%%" height="160%%">
                      <feDropShadow dx="0" dy="7" stdDeviation="8" flood-color="#55466f" flood-opacity=".2"/>
                    </filter>
                  </defs>
                  <rect width="640" height="400" rx="26" fill="url(#bg)"/>
                  <circle cx="525" cy="76" r="112" fill="#fff8df" opacity=".24"/>
                  <circle cx="600" cy="312" r="88" fill="#effff1" opacity=".2"/>
                  <g fill="none" stroke="#fff8e8" stroke-width="2" opacity=".62">
                    <path d="M224 58h80v34h49"/><path d="M232 297h75v-35h42"/>
                    <path d="M463 67h-45v35h-32"/><path d="M480 292h-56v-34h-39"/>
                  </g>
                  <g filter="url(#shadow)" transform="translate(49 80)">
                    <path d="M89 19 77 0l-9 21" fill="none" stroke="#fffdf5" stroke-width="9" stroke-linecap="round"/>
                    <path d="M42 55c0-24 20-42 47-42s47 18 47 42v51c0 27-20 46-47 46s-47-19-47-46Z"
                          fill="url(#robot)" stroke="#62cbb8" stroke-width="5"/>
                    <path d="m43 70-18 12 18 11m92-23 18 12-18 11" fill="none" stroke="#fffdf5" stroke-width="10" stroke-linecap="round" stroke-linejoin="round"/>
                    <rect x="57" y="61" width="64" height="43" rx="16" fill="#fff4df" stroke="#edaa7d" stroke-width="3"/>
                    <circle cx="76" cy="79" r="6" fill="#e99c76"/><circle cx="102" cy="79" r="6" fill="#e99c76"/>
                    <path d="M79 91q10 7 20 0" fill="none" stroke="#cd806d" stroke-width="3" stroke-linecap="round"/>
                    <path d="M65 151v16m48-16v16" stroke="#55b9ab" stroke-width="6" stroke-linecap="round"/>
                    <rect x="68" y="164" width="42" height="31" rx="12" fill="#fff4df" stroke="#edaa7d" stroke-width="3"/>
                    <path d="m79 180 5-5-5-5m14 10 5-5-5-5" fill="none" stroke="#cd806d" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </g>
                  <g filter="url(#shadow)" transform="translate(411 57) rotate(7)">
                    <rect width="163" height="113" rx="12" fill="%s" stroke="#fff5df" stroke-width="2"/>
                    <path d="M0 23h163" stroke="#fff8e8" stroke-width="2" opacity=".75"/>
                    <circle cx="15" cy="12" r="3" fill="#fff5df"/><circle cx="27" cy="12" r="3" fill="#fff5df"/><circle cx="39" cy="12" r="3" fill="#fff5df"/>
                    <path d="m26 51-9 8 9 8m23-16 9 8-9 8m25-16-7 16" fill="none" stroke="#fffdf2" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
                    <rect x="89" y="46" width="55" height="5" rx="2.5" fill="#fff8e8"/>
                    <rect x="89" y="62" width="44" height="5" rx="2.5" fill="#fffdf2"/>
                    <rect x="26" y="88" width="118" height="5" rx="2.5" fill="#fff8e8" opacity=".85"/>
                  </g>
                  <g filter="url(#shadow)" transform="translate(391 220) rotate(-5)">
                    <path d="M10 0h171l-12 91H0Z" fill="#fff9eb" fill-opacity=".86" stroke="%s" stroke-width="2"/>
                    <path d="M25 24h119M23 40h131M21 56h96" stroke="%s" stroke-width="4" stroke-linecap="round" opacity=".8"/>
                    <circle cx="45" cy="74" r="8" fill="#81d8bb"/><path d="m42 74 2 2 5-5" fill="none" stroke="#fff" stroke-width="2"/>
                    <rect x="60" y="72" width="91" height="5" rx="2.5" fill="%s" opacity=".65"/>
                  </g>
                  <g fill="#fff8df"><path d="m258 49 5 12 12 5-12 5-5 12-5-12-12-5 12-5z"/><path d="m585 190 4 9 9 4-9 4-4 9-4-9-9-4 9-4z"/></g>
                  <path d="M0 333c92-27 179 17 273-1 134-27 225-8 367-33v101H0Z" fill="#fff4df" opacity=".38"/>
                  %s
                  <text x="320" y="311" fill="#fffaf0" font-family="Microsoft YaHei, Noto Sans CJK SC, sans-serif"
                        font-size="16" font-weight="600" text-anchor="middle">开启课程学习之旅</text>
                  <rect x="0" y="367" width="640" height="33" fill="#fffaf0" opacity=".93"/>
                  <text x="320" y="389" fill="#6d6489" font-family="Microsoft YaHei, Noto Sans CJK SC, sans-serif"
                        font-size="13" text-anchor="middle">探索知识 · 学习成长 · 收获新技能</text>
                </svg>
                """.formatted(safeTitle, palette, palette, palette, palette, palette, titleMarkup);

        Files.createDirectories(UPLOAD_DIRECTORY);
        String storedFilename = UUID.randomUUID() + ".svg";
        Files.writeString(UPLOAD_DIRECTORY.resolve(storedFilename), svg, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
        return "/uploads/" + storedFilename;
    }

    private List<String> splitTitle(String title) {
        List<String> lines = new ArrayList<>();
        int maxCodePointsPerLine = 14;
        int offset = 0;
        while (offset < title.length() && lines.size() < 2) {
            int end = title.offsetByCodePoints(offset,
                    Math.min(maxCodePointsPerLine, title.codePointCount(offset, title.length())));
            lines.add(title.substring(offset, end));
            offset = end;
        }
        if (offset < title.length()) {
            String lastLine = lines.get(lines.size() - 1);
            int visibleCodePoints = lastLine.codePointCount(0, lastLine.length()) - 1;
            int end = lastLine.offsetByCodePoints(0, visibleCodePoints);
            lines.set(lines.size() - 1, lastLine.substring(0, end) + "…");
        }
        return lines;
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String saveCover(MultipartFile cover) throws IOException {
        String originalFilename = cover.getOriginalFilename();
        String filename = originalFilename == null ? "" : Paths.get(originalFilename).getFileName().toString();
        int extensionIndex = filename.lastIndexOf('.');
        String extension = extensionIndex >= 0 ? filename.substring(extensionIndex).toLowerCase() : ".jpg";
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("封面仅支持 JPG、PNG、GIF 或 WebP 图片");
        }

        Files.createDirectories(UPLOAD_DIRECTORY);
        String storedFilename = UUID.randomUUID() + extension;
        cover.transferTo(UPLOAD_DIRECTORY.resolve(storedFilename));
        return "/uploads/" + storedFilename;
    }
}
