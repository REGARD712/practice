package com.petadoption.service;

import com.petadoption.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 宠物图片的本地文件存储服务。
 * 负责上传目录初始化、文件类型与大小校验、随机命名保存、删除以及批量保存失败时的补偿清理。
 */
@Service
public class FileStorageService {
    /** 记录图片删除失败等存储异常，避免敏感文件内容进入日志。 */
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    /** 单张图片允许的最大字节数：2MB。 */
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    /** 声明允许的 MIME 类型及其对应扩展名，保存前要求两者匹配。 */
    private static final Map<String, Set<String>> ALLOWED_TYPES = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp"),
            "image/gif", Set.of("gif")
    );

    /** 经绝对化和规范化后的实际上传根目录。 */
    private final Path uploadDirectory;

    /**
     * 根据配置创建文件存储服务，并确保上传目录可用。
     *
     * @param uploadDir 配置项 app.upload-dir 指定的上传目录
     */
    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        // 转为绝对路径并消除 .、.. 等路径片段，后续解析和父目录校验均以此为准。
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            // 递归创建不存在的目录；目录已存在时不会重复创建。
            Files.createDirectories(uploadDirectory);
        } catch (IOException ex) {
            // 初始化失败时立即终止 Bean 创建，避免应用运行后才发现无法保存图片。
            throw new IllegalStateException("无法初始化图片上传目录", ex);
        }
    }

    /**
     * 暴露规范化上传目录，供 MVC 静态资源映射使用。
     *
     * @return 本地绝对上传目录
     */
    public Path getUploadDirectory() {
        return uploadDirectory;
    }

    /**
     * 校验并依次保存一组图片。
     * 文件系统不参与数据库事务，因此任一文件处理失败都会主动删除本批次已记录的路径，避免遗留孤儿文件。
     *
     * @param files 请求中的文件列表，可以包含 null 或空文件占位项
     * @param required 是否要求至少存在一张非空图片
     * @return 可存入数据库并用于页面访问的 /uploads/ 相对 URL 列表
     */
    public List<String> storeImages(List<MultipartFile> files, boolean required) {
        // 过滤空文件及 null 占位项；整个参数为 null 时使用不可变空列表。
        List<MultipartFile> effectiveFiles = files == null ? List.of() : files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
        // 创建宠物等必须配图的场景，在写盘前检查有效文件数量。
        if (required && effectiveFiles.isEmpty()) {
            throw new BusinessException("请至少上传一张有效的宠物图片");
        }

        // 按处理顺序记录待保存及已保存 URL，供正常返回和异常补偿共同使用。
        List<String> storedPaths = new ArrayList<>();
        try {
            // 逐张校验和保存，前一张成功后才处理下一张。
            for (MultipartFile file : effectiveFiles) {
                // 检查非空、大小上限以及 MIME 类型与扩展名是否匹配。
                validate(file);
                // 仅从原始文件名提取扩展名，保存文件名不采用用户输入。
                String extension = extensionOf(file.getOriginalFilename());
                // 使用 UUID 生成不可预测的独立文件名，降低重名覆盖和路径注入风险。
                String storedPath = "/uploads/" + UUID.randomUUID() + "." + extension;
                // 去掉 URL 前缀后在上传根目录下解析目标，并再次规范化路径。
                Path target = uploadDirectory.resolve(storedPath.substring("/uploads/".length())).normalize();
                // 在复制前登记路径，即使复制中断，补偿逻辑也会尝试删除可能不完整的目标文件。
                storedPaths.add(storedPath);
                // 使用 try-with-resources 确保上传输入流在成功或异常时均关闭。
                try (InputStream inputStream = file.getInputStream()) {
                    // 将文件内容复制到随机目标路径；默认不覆盖同名已有文件。
                    Files.copy(inputStream, target);
                }
            }
            // 全部图片处理成功后，按原顺序返回访问路径。
            return storedPaths;
        } catch (RuntimeException ex) {
            // 校验异常或其他运行时异常发生时，清理本批次已经写入或正在写入的文件。
            deleteStoredFiles(storedPaths);
            // 保留原异常类型和消息，由上层继续处理。
            throw ex;
        } catch (IOException ex) {
            // 读写失败同样先执行文件补偿，避免数据库操作尚未开始就留下垃圾文件。
            deleteStoredFiles(storedPaths);
            // 将底层 I/O 异常转换为可反馈给用户的业务异常，并保留原因链。
            throw new BusinessException("图片保存失败，请稍后重试", ex);
        }
    }

    /**
     * 删除上传根目录直接包含的一张图片。
     * 仅接受 /uploads/ 路径且要求规范化后仍位于上传根目录的直接子级，阻止借助 ../ 删除外部文件。
     *
     * @param imagePath 数据库中保存的图片访问路径
     */
    public void deleteUploadedImage(String imagePath) {
        // 空路径或非本服务管理的路径不执行文件系统操作。
        if (imagePath == null || !imagePath.startsWith("/uploads/")) {
            return;
        }
        // 去掉 URL 前缀，得到相对于上传根目录的文件名。
        String relativePath = imagePath.substring("/uploads/".length());
        // 解析并规范化目标路径，使 .. 等片段在校验前被展开。
        Path target = uploadDirectory.resolve(relativePath).normalize();
        // 只允许删除上传目录下的直接子文件，拒绝越界路径和额外子目录。
        if (!target.getParent().equals(uploadDirectory)) {
            return;
        }
        try {
            // 删除存在的文件；文件已被清理时不视为错误。
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            // 删除失败只记录警告，避免物理文件清理问题掩盖主要业务结果。
            log.warn("无法删除已上传图片: {}", imagePath);
        }
    }

    /**
     * 校验单张上传图片。
     *
     * @param file 当前待保存的上传文件
     */
    private void validate(MultipartFile file) {
        // 拒绝空上传项和零字节文件。
        if (file.isEmpty() || file.getSize() == 0) {
            throw new BusinessException("不能上传空图片");
        }
        // 在复制前按声明大小限制单张图片不超过 2MB。
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("单张图片不能超过2MB");
        }
        // 读取客户端声明的 Content-Type，下一步移除 charset 等参数并统一大小写。
        String contentType = file.getContentType();
        // 仅保留分号前的 MIME 主值，例如 image/jpeg; charset=... 归为 image/jpeg。
        contentType = contentType == null ? "" : contentType.split(";", 2)[0].toLowerCase(Locale.ROOT).trim();
        // 查找该 MIME 类型允许的扩展名集合；不支持时结果为 null。
        Set<String> extensions = ALLOWED_TYPES.get(contentType);
        // 从原始文件名提取并规范化扩展名。
        String extension = extensionOf(file.getOriginalFilename());
        // MIME 类型必须在白名单内，且扩展名必须属于该类型，不能任意混用。
        if (extensions == null || !extensions.contains(extension)) {
            throw new BusinessException("仅支持 JPEG、PNG、WEBP、GIF 图片，且文件类型与扩展名必须一致");
        }
    }

    /**
     * 从文件名末尾提取扩展名。
     *
     * @param filename 原始文件名，不用于生成保存路径
     * @return 小写扩展名；没有文件名、没有点号或点号位于末尾时返回空串
     */
    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        // 使用最后一个点号定位扩展名，允许文件名本身包含多个点号。
        int dotIndex = filename.lastIndexOf('.');
        // 无扩展名或以点号结尾均视为无有效扩展名。
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        // 扩展名统一转为小写，以便与白名单稳定比较。
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 对一组存储路径逐项执行补偿删除。
     *
     * @param storedPaths 本批次已登记的图片访问路径
     */
    private void deleteStoredFiles(List<String> storedPaths) {
        // 复用单文件删除的路径安全校验及异常处理。
        for (String storedPath : storedPaths) {
            deleteUploadedImage(storedPath);
        }
    }
}
