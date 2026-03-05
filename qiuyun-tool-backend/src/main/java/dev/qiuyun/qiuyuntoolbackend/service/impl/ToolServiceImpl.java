package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolFile;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolTask;
import dev.qiuyun.qiuyuntoolbackend.enums.TaskStatus;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutorRegistry;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.payload.request.FileProcessRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.request.ToolExecuteRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.FileUploadResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolExecuteResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolProgress;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolFileRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolTaskRepository;
import dev.qiuyun.qiuyuntoolbackend.service.FileStorageService;
import dev.qiuyun.qiuyuntoolbackend.service.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final ToolTaskRepository taskRepository;
    private final ToolFileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final ToolExecutorRegistry executorRegistry;

    private final Map<String, SseEmitter> progressEmitters = new ConcurrentHashMap<>();

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String toolCode) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        try {
            Path filePath = fileStorageService.saveTempFile(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    toolCode
            );

            ToolFile toolFile = fileRepository.findByStoragePath(filePath.toString())
                    .orElseThrow(() -> new BusinessException("文件保存失败"));

            return fileStorageService.createFileResponse(toolFile);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileStream(String fileId) {
        return fileStorageService.getFileStream(fileId);
    }

    @Override
    public void deleteFile(String fileId) {
        fileStorageService.deleteFile(fileId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, R> ToolExecuteResponse<R> execute(ToolExecuteRequest<T> request, Long userId) {
        String toolCode = request.getToolCode();

        ToolExecutor<T, R> executor = (ToolExecutor<T, R>) executorRegistry.getExecutor(toolCode);
        if (executor == null) {
            throw new BusinessException("工具不存在: " + toolCode);
        }

        String taskId = UUID.randomUUID().toString();

        ToolTask task = ToolTask.builder()
                .taskId(taskId)
                .toolCode(toolCode)
                .userId(userId)
                .status(TaskStatus.PENDING)
                .progress(0)
                .inputParams(request.getParams())
                .build();
        taskRepository.save(task);

        ToolType toolType = executor.getToolType();

        if (toolType == ToolType.INSTANT) {
            return executeInstant(task, executor, request);
        } else if (toolType == ToolType.FILE_PROCESS) {
            return executeFileProcess(task, executor, request);
        } else {
            return executeAsync(task, executor, request);
        }
    }

    private <T, R> ToolExecuteResponse<R> executeInstant(ToolTask task, ToolExecutor<T, R> executor, ToolExecuteRequest<T> request) {
        try {
            task.setStatus(TaskStatus.PROCESSING);
            taskRepository.save(task);

            T params = request.getParams();
            executor.validate(params);

            ToolContext context = ToolContext.builder()
                    .taskId(task.getTaskId())
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getToolDir(task.getToolCode()))
                    .build();

            R result = executor.execute(params, context);

            task.setStatus(TaskStatus.COMPLETED);
            task.setProgress(100);
            task.setOutputResult(result);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);

            return ToolExecuteResponse.<R>builder()
                    .taskId(task.getTaskId())
                    .status(TaskStatus.COMPLETED.getCode())
                    .result(result)
                    .progress(100)
                    .message("执行成功")
                    .build();

        } catch (BusinessException e) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            taskRepository.save(task);
            throw e;
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("执行失败: " + e.getMessage());
            taskRepository.save(task);
            throw new BusinessException("执行失败: " + e.getMessage());
        }
    }

    private <T, R> ToolExecuteResponse<R> executeFileProcess(ToolTask task, ToolExecutor<T, R> executor, ToolExecuteRequest<T> request) {
        try {
            task.setStatus(TaskStatus.PROCESSING);
            taskRepository.save(task);

            T params = request.getParams();

            // 检查文件处理请求是否包含 fileId
            if (params instanceof FileProcessRequest) {
                FileProcessRequest fileRequest = (FileProcessRequest) params;
                if (fileRequest.getFileId() == null || fileRequest.getFileId().trim().isEmpty()) {
                    throw new BusinessException("文件ID不能为空");
                }
            }

            executor.validate(params);

            SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
            progressEmitters.put(task.getTaskId(), emitter);

            ToolContext context = ToolContext.builder()
                    .taskId(task.getTaskId())
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getToolDir(task.getToolCode()))
                    .progressCallback(progress -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("progress")
                                    .data(progress));
                            task.setProgress(progress.getPercent());
                            taskRepository.save(task);
                        } catch (IOException e) {
                            log.warn("Failed to send progress: {}", e.getMessage());
                        }
                    })
                    .build();

            R result = executor.execute(params, context);

            task.setStatus(TaskStatus.COMPLETED);
            task.setProgress(100);
            task.setOutputResult(result);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);

            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(ToolProgress.complete("处理完成")));
            emitter.complete();

            return ToolExecuteResponse.<R>builder()
                    .taskId(task.getTaskId())
                    .status(TaskStatus.COMPLETED.getCode())
                    .result(result)
                    .progress(100)
                    .message("执行成功")
                    .build();

        } catch (BusinessException e) {
            handleTaskFailure(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            handleTaskFailure(task, "执行失败: " + e.getMessage());
            throw new BusinessException("执行失败: " + e.getMessage());
        }
    }

    @Async
    private <T, R> void executeAsyncInternal(ToolTask task, ToolExecutor<T, R> executor, ToolExecuteRequest<T> request) {
        SseEmitter emitter = progressEmitters.get(task.getTaskId());
        try {
            T params = request.getParams();
            executor.validate(params);

            ToolContext context = ToolContext.builder()
                    .taskId(task.getTaskId())
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getToolDir(task.getToolCode()))
                    .progressCallback(progress -> {
                        try {
                            if (emitter != null) {
                                emitter.send(SseEmitter.event()
                                        .name("progress")
                                        .data(progress));
                            }
                            task.setProgress(progress.getPercent());
                            taskRepository.save(task);
                        } catch (IOException e) {
                            log.warn("Failed to send progress: {}", e.getMessage());
                        }
                    })
                    .build();

            R result = executor.execute(params, context);

            task.setStatus(TaskStatus.COMPLETED);
            task.setProgress(100);
            task.setOutputResult(result);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);

            if (emitter != null) {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(ToolProgress.complete("处理完成")));
                emitter.complete();
            }

        } catch (Exception e) {
            handleTaskFailure(task, e.getMessage());
        }
    }

    private <T, R> ToolExecuteResponse<R> executeAsync(ToolTask task, ToolExecutor<T, R> executor, ToolExecuteRequest<T> request) {
        task.setStatus(TaskStatus.PROCESSING);
        taskRepository.save(task);

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        progressEmitters.put(task.getTaskId(), emitter);

        executeAsyncInternal(task, executor, request);

        return ToolExecuteResponse.<R>builder()
                .taskId(task.getTaskId())
                .status(TaskStatus.PROCESSING.getCode())
                .progress(0)
                .message("任务已提交")
                .build();
    }

    private void handleTaskFailure(ToolTask task, String errorMessage) {
        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        taskRepository.save(task);

        SseEmitter emitter = progressEmitters.get(task.getTaskId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(ToolProgress.error(errorMessage)));
                emitter.complete();
            } catch (IOException e) {
                log.warn("Failed to send error: {}", e.getMessage());
            }
        }
    }

    @Override
    public ToolExecuteResponse<Object> getTaskStatus(String taskId) {
        ToolTask task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        return ToolExecuteResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus().getCode())
                .progress(task.getProgress())
                .result(task.getOutputResult())
                .message(task.getErrorMessage())
                .build();
    }

    @Override
    public SseEmitter getProgressEmitter(String taskId) {
        ToolTask task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.FAILED) {
            SseEmitter emitter = new SseEmitter();
            try {
                if (task.getStatus() == TaskStatus.COMPLETED) {
                    emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(ToolProgress.complete("处理完成")));
                } else {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(ToolProgress.error(task.getErrorMessage())));
                }
                emitter.complete();
            } catch (IOException e) {
                log.warn("Failed to send status: {}", e.getMessage());
            }
            return emitter;
        }

        SseEmitter existingEmitter = progressEmitters.get(taskId);
        if (existingEmitter != null) {
            return existingEmitter;
        }

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        progressEmitters.put(taskId, emitter);

        emitter.onCompletion(() -> progressEmitters.remove(taskId));
        emitter.onTimeout(() -> progressEmitters.remove(taskId));
        emitter.onError(e -> progressEmitters.remove(taskId));

        return emitter;
    }

    @Override
    public String getDownloadUrl(String taskId) {
        ToolTask task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new BusinessException("任务未完成");
        }

        return "/api/tools/tasks/" + taskId + "/download";
    }

    @Override
    public void cancelTask(String taskId) {
        ToolTask task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        if (task.getStatus() == TaskStatus.PROCESSING) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("用户取消");
            taskRepository.save(task);

            SseEmitter emitter = progressEmitters.get(taskId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(ToolProgress.error("用户取消")));
                    emitter.complete();
                } catch (IOException e) {
                    log.warn("Failed to send cancel: {}", e.getMessage());
                }
            }
        }
    }
}
