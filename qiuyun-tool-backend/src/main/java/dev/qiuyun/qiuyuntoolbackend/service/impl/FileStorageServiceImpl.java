package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolFile;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolFileRepository;
import dev.qiuyun.qiuyuntoolbackend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务实现类
 * 提供文件存储、管理、访问等功能的具体实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    /**
     * 临时目录路径，默认使用系统临时目录下的 qiuyun-tools 目录
     */
    @Value("${tool.temp.dir:${java.io.tmpdir}/qiuyun-tools}")
    private String tempDirPath;

    /**
     * 文件最大大小限制，默认200MB
     */
    @Value("${tool.file.max-size:209715200}")
    private long maxFileSize;

    /**
     * 工具文件仓库，用于存储文件元数据
     */
    private final ToolFileRepository toolFileRepository;

    /**
     * 获取临时目录
     * @return 临时目录路径
     */
    @Override
    public Path getTempDir() {
        Path path = Paths.get(tempDirPath);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new BusinessException("创建临时目录失败: " + e.getMessage());
        }
        return path;
    }

    /**
     * 获取工具专用目录
     * @param toolCode 工具代码
     * @return 工具目录路径
     */
    @Override
    public Path getToolDir(String toolCode) {
        Path path = getTempDir().resolve(toolCode);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new BusinessException("创建工具目录失败: " + e.getMessage());
        }
        return path;
    }

    /**
     * 保存临时文件（从输入流）
     * @param inputStream 文件输入流
     * @param originalName 原始文件名
     * @param contentType 文件内容类型
     * @param toolCode 工具代码
     * @return 保存后的文件路径
     */
    @Override
    public Path saveTempFile(InputStream inputStream, String originalName, String contentType, String toolCode) {
        // 生成唯一文件ID
        String fileId = UUID.randomUUID().toString();
        // 获取文件扩展名
        String extension = getExtension(originalName);
        // 获取工具目录
        Path toolDir = getToolDir(toolCode);
        // 构建目标文件路径
        Path targetPath = toolDir.resolve(fileId + extension);

        try {
            // 保存文件
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 保存文件元数据
            ToolFile toolFile = ToolFile.builder()
                    .fileId(fileId)
                    .originalName(originalName)
                    .storagePath(targetPath.toString())
                    .fileSize(Files.size(targetPath))
                    .contentType(contentType)
                    .expireAt(LocalDateTime.now().plusDays(1)) // 1天过期
                    .build();
            toolFileRepository.save(toolFile);

            return targetPath;
        } catch (IOException e) {
            throw new BusinessException("保存文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存临时文件（从字节数组）
     * @param content 文件内容
     * @param originalName 原始文件名
     * @param contentType 文件内容类型
     * @param toolCode 工具代码
     * @return 保存后的文件路径
     */
    @Override
    public Path saveTempFile(byte[] content, String originalName, String contentType, String toolCode) {
        // 生成唯一文件ID
        String fileId = UUID.randomUUID().toString();
        // 获取文件扩展名
        String extension = getExtension(originalName);
        // 获取工具目录
        Path toolDir = getToolDir(toolCode);
        // 构建目标文件路径
        Path targetPath = toolDir.resolve(fileId + extension);

        try {
            // 保存文件
            Files.write(targetPath, content);

            // 保存文件元数据
            ToolFile toolFile = ToolFile.builder()
                    .fileId(fileId)
                    .originalName(originalName)
                    .storagePath(targetPath.toString())
                    .fileSize((long) content.length)
                    .contentType(contentType)
                    .expireAt(LocalDateTime.now().plusDays(1)) // 1天过期
                    .build();
            toolFileRepository.save(toolFile);

            return targetPath;
        } catch (IOException e) {
            throw new BusinessException("保存文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件输入流（从文件路径）
     * @param filePath 文件路径
     * @return 文件输入流
     */
    @Override
    public InputStream getFileStream(Path filePath) {
        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件输入流（从文件ID）
     * @param fileId 文件ID
     * @return 文件输入流
     */
    @Override
    public InputStream getFileStream(String fileId) {
        Path filePath = getFilePath(fileId);
        return getFileStream(filePath);
    }

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    @Override
    public void deleteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", filePath, e);
        }
    }

    /**
     * 删除任务相关文件
     * @param taskId 任务ID
     */
    @Override
    public void deleteTaskFiles(String taskId) {
        // 查询任务相关的文件
        List<ToolFile> files = toolFileRepository.findByTaskId(taskId);
        // 删除文件
        for (ToolFile file : files) {
            deleteFile(Paths.get(file.getStoragePath()));
        }
        // 删除文件元数据
        toolFileRepository.deleteByTaskId(taskId);
    }

    /**
     * 根据文件ID获取文件路径
     * @param fileId 文件ID
     * @return 文件路径
     */
    @Override
    public Path getFilePath(String fileId) {
        // 查询文件元数据
        ToolFile toolFile = toolFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));
        return Paths.get(toolFile.getStoragePath());
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 文件扩展名（包含点号）
     */
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 图片存储目录路径，默认使用系统临时目录下的 qiuyun-images 目录
     */
    @Value("${app.image.dir:${java.io.tmpdir}/qiuyun-images}")
    private String imageDirPath;

    /**
     * 图片URL前缀，默认 /api/images
     */
    @Value("${app.image.url-prefix:/api/images}")
    private String imageUrlPrefix;

    /**
     * 应用基础URL，默认 http://localhost:8080
     */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 存储评论图片
     * @param file 图片文件
     * @param userId 用户ID
     * @return 图片存储路径
     */
    @Override
    public String storeImage(MultipartFile file, Long userId) {
        try {
            // 创建图片存储目录
            Path imageDir = Paths.get(imageDirPath);
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            // 生成文件名: 日期/用户ID_随机UUID.扩展名
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            Path datePath = imageDir.resolve(dateDir);
            if (!Files.exists(datePath)) {
                Files.createDirectories(datePath);
            }

            String extension = getExtension(file.getOriginalFilename());
            String fileName = String.format("%s_%s%s", userId, UUID.randomUUID().toString().substring(0, 8), extension);
            Path targetPath = datePath.resolve(fileName);

            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径
            return dateDir + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException("保存图片失败: " + e.getMessage());
        }
    }

    /**
     * 获取图片访问URL
     * @param fileName 图片文件名
     * @return 图片访问URL
     */
    @Override
    public String getImageUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        // 返回完整URL，包含baseUrl
        return baseUrl + imageUrlPrefix + "/" + fileName;
    }

    /**
     * 获取图片文件流
     * @param fileName 图片文件名
     * @return 图片文件流
     */
    @Override
    public InputStream getImageStream(String fileName) {
        try {
            Path imagePath = Paths.get(imageDirPath).resolve(fileName);
            return Files.newInputStream(imagePath);
        } catch (IOException e) {
            throw new BusinessException("读取图片失败: " + e.getMessage());
        }
    }

    /**
     * 删除图片
     * @param imageUrl 图片URL
     */
    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        try {
            // 从URL中提取文件名
            String fileName = imageUrl;
            if (imageUrl.contains(imageUrlPrefix)) {
                fileName = imageUrl.substring(imageUrl.indexOf(imageUrlPrefix) + imageUrlPrefix.length() + 1);
            }
            
            Path imagePath = Paths.get(imageDirPath).resolve(fileName);
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                log.debug("删除图片成功: {}", imagePath);
            }
        } catch (IOException e) {
            log.warn("删除图片失败: {}, 原因: {}", imageUrl, e.getMessage());
        }
    }
}
