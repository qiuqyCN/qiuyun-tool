package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolExecuteResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.InputStream;
import java.util.List;

/**
 * 工具服务接口
 * 提供工具执行、任务管理、文件操作等核心功能
 */
public interface ToolService {

    /**
     * 获取文件输入流
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream getFileStream(String fileId);

    /**
     * 执行工具
     * @param toolCode 工具代码
     * @param paramsJson JSON格式的工具参数
     * @param files 上传的文件列表
     * @param userId 用户ID
     * @param <T> 请求参数类型
     * @param <R> 响应结果类型
     * @return 工具执行响应
     */
    <T, R> ToolExecuteResponse<R> execute(String toolCode, String paramsJson, List<MultipartFile> files, Long userId);

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态响应
     */
    ToolExecuteResponse<Object> getTaskStatus(String taskId);

    /**
     * 获取进度SSE发射器
     * @param taskId 任务ID
     * @return SSE发射器
     */
    SseEmitter getProgressEmitter(String taskId);

    /**
     * 获取任务结果下载URL
     * @param taskId 任务ID
     * @return 下载URL
     */
    String getDownloadUrl(String taskId);

    /**
     * 取消任务
     * @param taskId 任务ID
     */
    void cancelTask(String taskId);

    /**
     * 根据代码获取工具详情
     * @param code 工具代码
     * @return 工具详情
     */
    ToolResponse getToolByCode(String code);
}
