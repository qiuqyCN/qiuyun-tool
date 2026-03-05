package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.request.ToolExecuteRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.FileUploadResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolExecuteResponse;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.ToolService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam @NotBlank String toolCode) {
        FileUploadResponse response = toolService.uploadFile(file, toolCode);
        return ApiResponse.success(response);
    }

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

    @DeleteMapping("/files/{fileId}")
    public ApiResponse<Void> deleteFile(@PathVariable String fileId) {
        toolService.deleteFile(fileId);
        return ApiResponse.success();
    }

    @PostMapping("/execute")
    public ApiResponse<ToolExecuteResponse<Object>> execute(
            @Valid @RequestBody ToolExecuteRequest<Object> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        ToolExecuteResponse<Object> response = toolService.execute(request, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<ToolExecuteResponse<Object>> getTaskStatus(@PathVariable String taskId) {
        ToolExecuteResponse<Object> response = toolService.getTaskStatus(taskId);
        return ApiResponse.success(response);
    }

    @GetMapping(value = "/tasks/{taskId}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable String taskId) {
        return toolService.getProgressEmitter(taskId);
    }

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

    @PostMapping("/tasks/{taskId}/cancel")
    public ApiResponse<Void> cancelTask(@PathVariable String taskId) {
        toolService.cancelTask(taskId);
        return ApiResponse.success();
    }
}
