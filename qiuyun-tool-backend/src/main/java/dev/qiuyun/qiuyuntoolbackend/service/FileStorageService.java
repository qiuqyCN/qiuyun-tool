package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolFile;
import dev.qiuyun.qiuyuntoolbackend.payload.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

public interface FileStorageService {

    Path getTempDir();

    Path getToolDir(String toolCode);

    Path saveTempFile(InputStream inputStream, String originalName, String contentType, String toolCode);

    Path saveTempFile(byte[] content, String originalName, String contentType, String toolCode);

    InputStream getFileStream(Path filePath);

    InputStream getFileStream(String fileId);

    void deleteFile(Path filePath);

    void deleteFile(String fileId);

    void deleteTaskFiles(String taskId);

    Path getFilePath(String fileId);

    FileUploadResponse createFileResponse(ToolFile toolFile);

    /**
     * 存储评论图片
     */
    String storeImage(MultipartFile file, Long userId);

    /**
     * 获取图片访问URL
     */
    String getImageUrl(String fileName);

    /**
     * 获取图片文件流
     */
    InputStream getImageStream(String fileName);
}
