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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
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
}
