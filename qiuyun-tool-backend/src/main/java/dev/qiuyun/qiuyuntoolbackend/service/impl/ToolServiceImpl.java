package dev.qiuyun.qiuyuntoolbackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolTask;
import dev.qiuyun.qiuyuntoolbackend.enums.TaskStatus;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutorRegistry;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolExecuteResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolProgress;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolTaskRepository;
import dev.qiuyun.qiuyuntoolbackend.service.FileStorageService;
import dev.qiuyun.qiuyuntoolbackend.service.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具服务实现类
 * 负责工具执行、任务管理、进度跟踪等核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final ToolTaskRepository taskRepository;
    private final ToolRepository toolRepository;
    private final FileStorageService fileStorageService;
    private final ToolExecutorRegistry executorRegistry;
    private final ObjectMapper objectMapper;

    /**
     * 进度发射器映射表，用于管理SSE连接
     * key: taskId, value: SseEmitter
     */
    private final Map<String, SseEmitter> progressEmitters = new ConcurrentHashMap<>();

    /**
     * 获取文件输入流
     * @param fileId 文件ID
     * @return 文件输入流
     */
    @Override
    public InputStream getFileStream(String fileId) {
        return fileStorageService.getFileStream(fileId);
    }

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
    @Override
    public <T, R> ToolExecuteResponse<R> execute(String toolCode, String paramsJson, List<MultipartFile> files, Long userId) {
        ToolExecutor<T, R> executor = executorRegistry.getExecutor(toolCode);
        if (executor == null) {
            throw new BusinessException("工具不存在: " + toolCode);
        }

        String taskId = UUID.randomUUID().toString();

        List<Path> inputFiles = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            try {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        Path filePath = fileStorageService.saveTempFile(
                                file.getInputStream(),
                                file.getOriginalFilename(),
                                file.getContentType(),
                                toolCode
                        );
                        inputFiles.add(filePath);
                    }
                }
            } catch (IOException e) {
                throw new BusinessException("文件保存失败: " + e.getMessage());
            }
        }

        T convertedParams = null;
        if (paramsJson != null && !paramsJson.trim().isEmpty()) {
            convertedParams = convertParams(paramsJson, executor);
        }

        ToolTask task = ToolTask.builder()
                .taskId(taskId)
                .toolCode(toolCode)
                .userId(userId)
                .status(TaskStatus.PENDING)
                .progress(0)
                .inputParams(convertedParams)
                .build();
        taskRepository.save(task);

        ToolType toolType = executor.getToolType();

        if (toolType == ToolType.INSTANT) {
            return executeInstant(task, executor, convertedParams, inputFiles);
        } else if (toolType == ToolType.FILE_PROCESS) {
            return executeFileProcess(task, executor, convertedParams, inputFiles);
        } else {
            return executeAsync(task, executor, convertedParams, inputFiles);
        }
    }

    /**
     * 将JSON参数转换为工具执行器所需的参数类型
     * @param paramsJson JSON格式的参数
     * @param executor 工具执行器
     * @param <T> 参数类型
     * @param <R> 结果类型
     * @return 转换后的参数对象
     */
    @SuppressWarnings("unchecked")
    private <T, R> T convertParams(String paramsJson, ToolExecutor<T, R> executor) {
        if (paramsJson == null || paramsJson.trim().isEmpty()) {
            return null;
        }

        ResolvableType resolvableType = ResolvableType.forClass(executor.getClass()).as(ToolExecutor.class);
        Class<?> requestType = resolvableType.getGeneric(0).resolve();

        if (requestType == null) {
            return (T) paramsJson;
        }

        try {
            return objectMapper.readValue(paramsJson, (Class<T>) requestType);
        } catch (Exception e) {
            log.error("参数转换失败: {}", e.getMessage());
            throw new BusinessException("参数格式错误: " + e.getMessage());
        }
    }

    /**
     * 执行即时类型工具（同步执行，立即返回结果）
     * @param task 任务实体
     * @param executor 工具执行器
     * @param params 工具参数
     * @param inputFiles 输入文件列表
     * @param <T> 参数类型
     * @param <R> 结果类型
     * @return 工具执行响应
     */
    private <T, R> ToolExecuteResponse<R> executeInstant(ToolTask task, ToolExecutor<T, R> executor, T params, List<Path> inputFiles) {
        try {
            task.setStatus(TaskStatus.PROCESSING);
            taskRepository.save(task);

            executor.validate(params);

            ToolContext context = ToolContext.builder()
                    .taskId(task.getTaskId())
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getToolDir(task.getToolCode()))
                    .inputFiles(inputFiles)
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
            log.error("任务执行失败: {}", task.getId(), e);
            throw e;
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("执行失败: " + e.getMessage());
            taskRepository.save(task);
            log.error("任务执行失败: {}", task.getId(), e);
            throw new BusinessException("执行失败: " + e.getMessage());
        }
    }

    /**
     * 执行文件处理类型工具（同步执行，支持进度反馈）
     * @param task 任务实体
     * @param executor 工具执行器
     * @param params 工具参数
     * @param inputFiles 输入文件列表
     * @param <T> 参数类型
     * @param <R> 结果类型
     * @return 工具执行响应
     */
    private <T, R> ToolExecuteResponse<R> executeFileProcess(ToolTask task, ToolExecutor<T, R> executor, T params, List<Path> inputFiles) {
        try {
            task.setStatus(TaskStatus.PROCESSING);
            taskRepository.save(task);

            executor.validate(params);

            SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
            progressEmitters.put(task.getTaskId(), emitter);

            ToolContext context = ToolContext.builder()
                    .taskId(task.getTaskId())
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getToolDir(task.getToolCode()))
                    .inputFiles(inputFiles)
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

    /**
     * 异步执行工具（后台线程执行）
     * @param task 任务实体
     * @param executor 工具执行器
     * @param params 工具参数
     * @param inputFiles 输入文件列表
     * @param <T> 参数类型
     * @param <R> 结果类型
     */
    @Async
    protected <T, R> void executeAsyncInternal(ToolTask task, ToolExecutor<T, R> executor, T params, List<Path> inputFiles) {
        SseEmitter emitter = progressEmitters.get(task.getTaskId());
        try {
            executor.validate(params);

            ToolContext context = ToolContext.builder()
                    .taskId(task.getTaskId())
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getToolDir(task.getToolCode()))
                    .inputFiles(inputFiles)
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

    /**
     * 执行异步类型工具（立即返回，后台处理）
     * @param task 任务实体
     * @param executor 工具执行器
     * @param params 工具参数
     * @param inputFiles 输入文件列表
     * @param <T> 参数类型
     * @param <R> 结果类型
     * @return 工具执行响应（任务已提交）
     */
    private <T, R> ToolExecuteResponse<R> executeAsync(ToolTask task, ToolExecutor<T, R> executor, T params, List<Path> inputFiles) {
        task.setStatus(TaskStatus.PROCESSING);
        taskRepository.save(task);

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        progressEmitters.put(task.getTaskId(), emitter);

        executeAsyncInternal(task, executor, params, inputFiles);

        return ToolExecuteResponse.<R>builder()
                .taskId(task.getTaskId())
                .status(TaskStatus.PROCESSING.getCode())
                .progress(0)
                .message("任务已提交")
                .build();
    }

    /**
     * 处理任务失败
     * @param task 任务实体
     * @param errorMessage 错误信息
     */
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

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态响应
     */
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

    /**
     * 获取进度SSE发射器
     * @param taskId 任务ID
     * @return SSE发射器
     */
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

    /**
     * 获取任务结果下载URL
     * @param taskId 任务ID
     * @return 下载URL
     */
    @Override
    public String getDownloadUrl(String taskId) {
        ToolTask task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new BusinessException("任务未完成");
        }

        return "/api/tools/tasks/" + taskId + "/download";
    }

    /**
     * 取消任务
     * @param taskId 任务ID
     */
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

    /**
     * 根据代码获取工具详情
     * @param code 工具代码
     * @return 工具详情
     */
    @Override
    public ToolResponse getToolByCode(String code) {
        Tool tool = toolRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("工具不存在: " + code));
        return ToolResponse.from(tool);
    }
}
