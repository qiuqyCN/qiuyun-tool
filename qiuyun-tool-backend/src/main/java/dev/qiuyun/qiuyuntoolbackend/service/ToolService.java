package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.request.ToolExecuteRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.FileUploadResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolExecuteResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.InputStream;

public interface ToolService {

    FileUploadResponse uploadFile(MultipartFile file, String toolCode);

    InputStream getFileStream(String fileId);

    void deleteFile(String fileId);

    <T, R> ToolExecuteResponse<R> execute(ToolExecuteRequest<T> request, Long userId);

    ToolExecuteResponse<Object> getTaskStatus(String taskId);

    SseEmitter getProgressEmitter(String taskId);

    String getDownloadUrl(String taskId);

    void cancelTask(String taskId);
}
