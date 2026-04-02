package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
     * 文件存储根目录，默认使用用户主目录下的 qiuyun-tool/files
     */
    @Value("${tool.file.storage-dir:${user.home}/qiuyun-tool/files}")
    private String storageDirPath;

    /**
     * 文件最大大小限制，默认200MB
     */
    @Value("${tool.file.max-size:209715200}")
    private long maxFileSize;

    /**
     * 日期格式器（yyyyMMdd）
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 获取文件存储根目录
     * @return 根目录路径
     */
    @Override
    public Path getStorageDir() {
        Path path = Paths.get(storageDirPath);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建文件存储根目录: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建文件存储根目录失败: {}", path.toAbsolutePath(), e);
            throw new BusinessException("创建文件存储目录失败: " + e.getMessage());
        }
        return path;
    }

    /**
     * 获取任务目录
     * 格式：{baseDir}/{yyyyMMdd}/{toolCode}_{taskId}/
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 任务目录路径
     */
    @Override
    public Path getTaskDir(String toolCode, String taskId) {
        String dateDir = LocalDateTime.now().format(DATE_FORMATTER);
        Path taskDir = getStorageDir().resolve(dateDir).resolve(toolCode + "_" + taskId);
        try {
            if (!Files.exists(taskDir)) {
                Files.createDirectories(taskDir);
                log.info("创建任务目录: {}", taskDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建任务目录失败: {}, toolCode: {}, taskId: {}", taskDir.toAbsolutePath(), toolCode, taskId, e);
            throw new BusinessException("创建任务目录失败: " + e.getMessage());
        }
        return taskDir;
    }

    /**
     * 获取任务输入目录
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 输入目录路径
     */
    @Override
    public Path getTaskInputDir(String toolCode, String taskId) {
        Path inputDir = getTaskDir(toolCode, taskId).resolve("input");
        try {
            if (!Files.exists(inputDir)) {
                Files.createDirectories(inputDir);
                log.debug("创建任务输入目录: {}", inputDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建任务输入目录失败: {}, toolCode: {}, taskId: {}", inputDir.toAbsolutePath(), toolCode, taskId, e);
            throw new BusinessException("创建任务输入目录失败: " + e.getMessage());
        }
        return inputDir;
    }

    /**
     * 获取任务输出目录
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 输出目录路径
     */
    @Override
    public Path getTaskOutputDir(String toolCode, String taskId) {
        Path outputDir = getTaskDir(toolCode, taskId).resolve("output");
        try {
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                log.debug("创建任务输出目录: {}", outputDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建任务输出目录失败: {}, toolCode: {}, taskId: {}", outputDir.toAbsolutePath(), toolCode, taskId, e);
            throw new BusinessException("创建任务输出目录失败: " + e.getMessage());
        }
        return outputDir;
    }

    /**
     * 保存任务输入文件
     * @param inputStream 文件输入流
     * @param originalName 原始文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    @Override
    public Path saveTaskInputFile(InputStream inputStream, String originalName, String toolCode, String taskId) {
        Path inputDir = getTaskInputDir(toolCode, taskId);
        String extension = getExtension(originalName);
        String fileName = UUID.randomUUID().toString() + extension;
        Path targetPath = inputDir.resolve(fileName);

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("保存任务输入文件成功: {}, 原始文件名: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), originalName, toolCode, taskId);
            return targetPath;
        } catch (IOException e) {
            log.error("保存任务输入文件失败: {}, 原始文件名: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), originalName, toolCode, taskId, e);
            throw new BusinessException("保存任务输入文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存任务输入文件（从字节数组）
     * @param content 文件内容
     * @param originalName 原始文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    @Override
    public Path saveTaskInputFile(byte[] content, String originalName, String toolCode, String taskId) {
        Path inputDir = getTaskInputDir(toolCode, taskId);
        String extension = getExtension(originalName);
        String fileName = UUID.randomUUID().toString() + extension;
        Path targetPath = inputDir.resolve(fileName);

        try {
            Files.write(targetPath, content);
            log.info("保存任务输入文件成功: {}, 原始文件名: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), originalName, toolCode, taskId);
            return targetPath;
        } catch (IOException e) {
            log.error("保存任务输入文件失败: {}, 原始文件名: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), originalName, toolCode, taskId, e);
            throw new BusinessException("保存任务输入文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存任务输出文件
     * @param content 文件内容
     * @param fileName 文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    @Override
    public Path saveTaskOutputFile(byte[] content, String fileName, String toolCode, String taskId) {
        Path outputDir = getTaskOutputDir(toolCode, taskId);
        Path targetPath = outputDir.resolve(fileName);

        try {
            Files.write(targetPath, content);
            log.info("保存任务输出文件成功: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), toolCode, taskId);
            return targetPath;
        } catch (IOException e) {
            log.error("保存任务输出文件失败: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), toolCode, taskId, e);
            throw new BusinessException("保存任务输出文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存任务输出文件（从输入流）
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    @Override
    public Path saveTaskOutputFile(InputStream inputStream, String fileName, String toolCode, String taskId) {
        Path outputDir = getTaskOutputDir(toolCode, taskId);
        Path targetPath = outputDir.resolve(fileName);

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("保存任务输出文件成功: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), toolCode, taskId);
            return targetPath;
        } catch (IOException e) {
            log.error("保存任务输出文件失败: {}, toolCode: {}, taskId: {}",
                    targetPath.toAbsolutePath(), toolCode, taskId, e);
            throw new BusinessException("保存任务输出文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务输出文件路径
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @param fileName 文件名
     * @return 文件路径
     */
    @Override
    public Path getTaskOutputFilePath(String toolCode, String taskId, String fileName) {
        Path outputDir = getTaskOutputDir(toolCode, taskId);
        return outputDir.resolve(fileName);
    }

    /**
     * 获取任务输入文件列表
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 输入文件路径列表
     */
    @Override
    public List<Path> getTaskInputFiles(String toolCode, String taskId) {
        Path inputDir = getTaskInputDir(toolCode, taskId);
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    files.add(path);
                }
            }
        } catch (IOException e) {
            log.error("获取任务输入文件列表失败: {}, toolCode: {}, taskId: {}",
                    inputDir.toAbsolutePath(), toolCode, taskId, e);
        }
        return files;
    }

    /**
     * 删除任务目录
     * @param toolCode 工具代码
     * @param taskId 任务ID
     */
    @Override
    public void deleteTaskDir(String toolCode, String taskId) {
        String dateDir = LocalDateTime.now().format(DATE_FORMATTER);
        Path taskDir = getStorageDir().resolve(dateDir).resolve(toolCode + "_" + taskId);
        deleteDirectory(taskDir);
    }

    /**
     * 获取任务目录的绝对路径（用于日志）
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 绝对路径字符串
     */
    @Override
    public String getTaskDirAbsolutePath(String toolCode, String taskId) {
        return getTaskDir(toolCode, taskId).toAbsolutePath().toString();
    }

    /**
     * 获取文件输入流
     * @param filePath 文件路径
     * @return 文件输入流
     */
    @Override
    public InputStream getFileStream(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                log.error("文件不存在: {}", filePath.toAbsolutePath());
                throw new BusinessException("文件不存在: " + filePath.getFileName());
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath.toAbsolutePath(), e);
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    @Override
    public void deleteFile(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("删除文件成功: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.warn("删除文件失败: {}", filePath.toAbsolutePath(), e);
        }
    }

    /**
     * 递归删除目录
     * @param dir 目录路径
     */
    private void deleteDirectory(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("删除: {}", path.toAbsolutePath());
                        } catch (IOException e) {
                            log.warn("删除失败: {}", path.toAbsolutePath(), e);
                        }
                    });
            log.info("删除任务目录成功: {}", dir.toAbsolutePath());
        } catch (IOException e) {
            log.error("删除任务目录失败: {}", dir.toAbsolutePath(), e);
        }
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

    // ==================== 图片相关方法（保持不变）====================

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
            Path imageDir = Paths.get(imageDirPath);
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            Path datePath = imageDir.resolve(dateDir);
            if (!Files.exists(datePath)) {
                Files.createDirectories(datePath);
            }

            String extension = getExtension(file.getOriginalFilename());
            String fileName = String.format("%s_%s%s", userId, UUID.randomUUID().toString().substring(0, 8), extension);
            Path targetPath = datePath.resolve(fileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return dateDir + "/" + fileName;
        } catch (IOException e) {
            log.error("保存图片失败", e);
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
            log.error("读取图片失败: {}", fileName, e);
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
