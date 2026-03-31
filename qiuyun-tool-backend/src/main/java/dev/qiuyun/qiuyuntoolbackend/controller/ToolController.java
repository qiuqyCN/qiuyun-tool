package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolExecuteResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.ToolService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;

/**
 * 工具相关接口控制器
 * 提供工具执行、文件下载、任务管理等功能
 */
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    /**
     * 根据工具代码获取工具详情
     * @param code 工具代码
     * @return 工具详情响应
     */
    @GetMapping("/{code}")
    public ApiResponse<ToolResponse> getToolByCode(@PathVariable String code) {
        return ApiResponse.success(toolService.getToolByCode(code));
    }

    /**
     * 下载文件
     * @param fileId 文件ID
     * @return 文件流
     */
    @GetMapping("/files/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable String fileId) {
        InputStream inputStream = toolService.getFileStream(fileId);

        StreamingResponseBody responseBody = outputStream -> {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + fileId + "\"")
                .body(responseBody);
    }

    /**
     * 执行工具
     * @param toolCode 工具代码（必填）
     * @param params JSON格式的工具参数（可选）
     * @param files 上传的文件列表（可选）
     * @param userDetails 当前登录用户信息
     * @return 工具执行响应
     */
    @PostMapping(value = "/execute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ToolExecuteResponse<Object>> execute(
            @RequestParam @NotBlank String toolCode,
            @RequestParam(required = false) String params,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        ToolExecuteResponse<Object> response = toolService.execute(toolCode, params, files, userId);
        return ApiResponse.success(response);
    }

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态响应
     */
    @GetMapping("/tasks/{taskId}")
    public ApiResponse<ToolExecuteResponse<Object>> getTaskStatus(@PathVariable String taskId) {
        ToolExecuteResponse<Object> response = toolService.getTaskStatus(taskId);
        return ApiResponse.success(response);
    }

    /**
     * 获取任务执行进度（SSE流式响应）
     * @param taskId 任务ID
     * @return SSE发射器
     */
    @GetMapping(value = "/tasks/{taskId}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable String taskId) {
        return toolService.getProgressEmitter(taskId);
    }

    /**
     * 下载任务结果文件
     * @param taskId 任务ID
     * @return 结果文件流
     */
    @GetMapping("/tasks/{taskId}/download")
    public ResponseEntity<StreamingResponseBody> downloadResult(@PathVariable String taskId) {
        toolService.getDownloadUrl(taskId);

        InputStream inputStream = toolService.getFileStream(taskId);

        StreamingResponseBody responseBody = outputStream -> {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"result_" + taskId + "\"")
                .body(responseBody);
    }

    /**
     * 取消任务
     * @param taskId 任务ID
     * @return 空响应
     */
    @PostMapping("/tasks/{taskId}/cancel")
    public ApiResponse<Void> cancelTask(@PathVariable String taskId) {
        toolService.cancelTask(taskId);
        return ApiResponse.success();
    }
}
