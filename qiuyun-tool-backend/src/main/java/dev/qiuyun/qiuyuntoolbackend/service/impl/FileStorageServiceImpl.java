package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolFile;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.payload.response.FileUploadResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${tool.temp.dir:${java.io.tmpdir}/qiuyun-tools}")
    private String tempDirPath;

    @Value("${tool.file.max-size:209715200}")
    private long maxFileSize;

    private final ToolFileRepository toolFileRepository;

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

    @Override
    public Path saveTempFile(InputStream inputStream, String originalName, String contentType, String toolCode) {
        String fileId = UUID.randomUUID().toString();
        String extension = getExtension(originalName);
        Path toolDir = getToolDir(toolCode);
        Path targetPath = toolDir.resolve(fileId + extension);

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            ToolFile toolFile = ToolFile.builder()
                    .fileId(fileId)
                    .originalName(originalName)
                    .storagePath(targetPath.toString())
                    .fileSize(Files.size(targetPath))
                    .contentType(contentType)
                    .expireAt(LocalDateTime.now().plusDays(1))
                    .build();
            toolFileRepository.save(toolFile);

            return targetPath;
        } catch (IOException e) {
            throw new BusinessException("保存文件失败: " + e.getMessage());
        }
    }

    @Override
    public Path saveTempFile(byte[] content, String originalName, String contentType, String toolCode) {
        String fileId = UUID.randomUUID().toString();
        String extension = getExtension(originalName);
        Path toolDir = getToolDir(toolCode);
        Path targetPath = toolDir.resolve(fileId + extension);

        try {
            Files.write(targetPath, content);

            ToolFile toolFile = ToolFile.builder()
                    .fileId(fileId)
                    .originalName(originalName)
                    .storagePath(targetPath.toString())
                    .fileSize((long) content.length)
                    .contentType(contentType)
                    .expireAt(LocalDateTime.now().plusDays(1))
                    .build();
            toolFileRepository.save(toolFile);

            return targetPath;
        } catch (IOException e) {
            throw new BusinessException("保存文件失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileStream(Path filePath) {
        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileStream(String fileId) {
        Path filePath = getFilePath(fileId);
        return getFileStream(filePath);
    }

    @Override
    public void deleteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", filePath, e);
        }
    }

    @Override
    public void deleteFile(String fileId) {
        ToolFile toolFile = toolFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        Path filePath = Paths.get(toolFile.getStoragePath());
        deleteFile(filePath);
        toolFileRepository.delete(toolFile);
    }

    @Override
    public void deleteTaskFiles(String taskId) {
        List<ToolFile> files = toolFileRepository.findByTaskId(taskId);
        for (ToolFile file : files) {
            deleteFile(Paths.get(file.getStoragePath()));
        }
        toolFileRepository.deleteByTaskId(taskId);
    }

    @Override
    public Path getFilePath(String fileId) {
        ToolFile toolFile = toolFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));
        return Paths.get(toolFile.getStoragePath());
    }

    @Override
    public FileUploadResponse createFileResponse(ToolFile toolFile) {
        return FileUploadResponse.builder()
                .fileId(toolFile.getFileId())
                .fileName(toolFile.getOriginalName())
                .fileSize(toolFile.getFileSize())
                .contentType(toolFile.getContentType())
                .build();
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Value("${app.image.dir:${java.io.tmpdir}/qiuyun-images}")
    private String imageDirPath;

    @Value("${app.image.url-prefix:/api/images}")
    private String imageUrlPrefix;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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

    @Override
    public String getImageUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        // 返回完整URL，包含baseUrl
        return baseUrl + imageUrlPrefix + "/" + fileName;
    }

    @Override
    public InputStream getImageStream(String fileName) {
        try {
            Path imagePath = Paths.get(imageDirPath).resolve(fileName);
            return Files.newInputStream(imagePath);
        } catch (IOException e) {
            throw new BusinessException("读取图片失败: " + e.getMessage());
        }
    }

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
