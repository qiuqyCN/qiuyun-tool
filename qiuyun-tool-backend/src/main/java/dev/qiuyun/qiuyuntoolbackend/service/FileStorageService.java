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
     * 获取文件存储根目录
     * @return 根目录路径
     */
    Path getStorageDir();

    /**
     * 获取任务目录
     * 格式：{baseDir}/{yyyyMMdd}/{toolCode}_{taskId}/
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 任务目录路径
     */
    Path getTaskDir(String toolCode, String taskId);

    /**
     * 获取任务输入目录
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 输入目录路径
     */
    Path getTaskInputDir(String toolCode, String taskId);

    /**
     * 获取任务输出目录
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 输出目录路径
     */
    Path getTaskOutputDir(String toolCode, String taskId);

    /**
     * 保存任务输入文件
     * @param inputStream 文件输入流
     * @param originalName 原始文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    Path saveTaskInputFile(InputStream inputStream, String originalName, String toolCode, String taskId);

    /**
     * 保存任务输入文件（从字节数组）
     * @param content 文件内容
     * @param originalName 原始文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    Path saveTaskInputFile(byte[] content, String originalName, String toolCode, String taskId);

    /**
     * 保存任务输出文件
     * @param content 文件内容
     * @param fileName 文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    Path saveTaskOutputFile(byte[] content, String fileName, String toolCode, String taskId);

    /**
     * 保存任务输出文件（从输入流）
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 保存后的文件路径
     */
    Path saveTaskOutputFile(InputStream inputStream, String fileName, String toolCode, String taskId);

    /**
     * 获取任务输出文件路径
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @param fileName 文件名
     * @return 文件路径
     */
    Path getTaskOutputFilePath(String toolCode, String taskId, String fileName);

    /**
     * 获取任务输入文件列表
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 输入文件路径列表
     */
    java.util.List<Path> getTaskInputFiles(String toolCode, String taskId);

    /**
     * 删除任务目录
     * @param toolCode 工具代码
     * @param taskId 任务ID
     */
    void deleteTaskDir(String toolCode, String taskId);

    /**
     * 获取任务目录的绝对路径（用于日志）
     * @param toolCode 工具代码
     * @param taskId 任务ID
     * @return 绝对路径字符串
     */
    String getTaskDirAbsolutePath(String toolCode, String taskId);

    /**
     * 获取文件输入流
     * @param filePath 文件路径
     * @return 文件输入流
     */
    InputStream getFileStream(Path filePath);

    /**
     * 删除文件
     * @param filePath 文件路径
     */
    void deleteFile(Path filePath);

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
