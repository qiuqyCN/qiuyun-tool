package dev.qiuyun.qiuyuntoolbackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * 文件存储服务接口
 * 提供文件存储、管理、访问等功能
 */
public interface FileStorageService {

    /**
     * 获取临时目录
     * @return 临时目录路径
     */
    Path getTempDir();

    /**
     * 获取工具专用目录
     * @param toolCode 工具代码
     * @return 工具目录路径
     */
    Path getToolDir(String toolCode);

    /**
     * 保存临时文件（从输入流）
     * @param inputStream 文件输入流
     * @param originalName 原始文件名
     * @param contentType 文件内容类型
     * @param toolCode 工具代码
     * @return 保存后的文件路径
     */
    Path saveTempFile(InputStream inputStream, String originalName, String contentType, String toolCode);

    /**
     * 保存临时文件（从字节数组）
     * @param content 文件内容
     * @param originalName 原始文件名
     * @param contentType 文件内容类型
     * @param toolCode 工具代码
     * @return 保存后的文件路径
     */
    Path saveTempFile(byte[] content, String originalName, String contentType, String toolCode);

    /**
     * 获取文件输入流（从文件路径）
     * @param filePath 文件路径
     * @return 文件输入流
     */
    InputStream getFileStream(Path filePath);

    /**
     * 获取文件输入流（从文件ID）
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream getFileStream(String fileId);

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    void deleteFile(Path filePath);

    /**
     * 删除任务相关文件
     * @param taskId 任务ID
     */
    void deleteTaskFiles(String taskId);

    /**
     * 根据文件ID获取文件路径
     * @param fileId 文件ID
     * @return 文件路径
     */
    Path getFilePath(String fileId);

    /**
     * 存储评论图片
     * @param file 图片文件
     * @param userId 用户ID
     * @return 图片存储路径
     */
    String storeImage(MultipartFile file, Long userId);

    /**
     * 获取图片访问URL
     * @param fileName 图片文件名
     * @return 图片访问URL
     */
    String getImageUrl(String fileName);

    /**
     * 获取图片文件流
     * @param fileName 图片文件名
     * @return 图片文件流
     */
    InputStream getImageStream(String fileName);

    /**
     * 删除图片
     * @param imageUrl 图片URL
     */
    void deleteImage(String imageUrl);
}
