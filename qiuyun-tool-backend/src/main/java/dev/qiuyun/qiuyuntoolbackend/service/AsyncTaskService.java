package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolTask;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ProcessLogEntry;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolProgress;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步任务服务
 * 处理文件处理和异步任务的执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

    private final ToolTaskRepository taskRepository;
    private final FileStorageService fileStorageService;

    /**
     * 进度发射器映射表，用于管理SSE连接
     * key: taskId, value: SseEmitter
     */
    private final Map<String, SseEmitter> progressEmitters = new ConcurrentHashMap<>();

    /**
     * 注册SSE发射器
     * @param taskId 任务ID
     * @param emitter SSE发射器
     */
    public void registerEmitter(String taskId, SseEmitter emitter) {
        progressEmitters.put(taskId, emitter);
    }

    /**
     * 移除SSE发射器
     * @param taskId 任务ID
     */
    public void removeEmitter(String taskId) {
        progressEmitters.remove(taskId);
    }

    /**
     * 获取SSE发射器
     * @param taskId 任务ID
     * @return SSE发射器
     */
    public SseEmitter getEmitter(String taskId) {
        return progressEmitters.get(taskId);
    }

    /**
     * 执行文件处理任务（异步）
     * @param task 任务实体
     * @param executor 工具执行器
     * @param params 工具参数
     * @param inputFiles 输入文件列表
     * @param <T> 参数类型
     * @param <R> 结果类型
     */
    @Async
    public <T, R> void executeFileProcess(ToolTask task, ToolExecutor<T, R> executor, T params, List<Path> inputFiles) {
        String taskId = task.getTaskId();
        String toolCode = task.getToolCode();
        SseEmitter emitter = progressEmitters.get(taskId);

        // 初始化日志列表
        List<ProcessLogEntry> logs = new ArrayList<>();

        log.info("开始执行文件处理任务, taskId: {}, toolCode: {}, 任务目录: {}",
                taskId, toolCode, fileStorageService.getTaskDirAbsolutePath(toolCode, taskId));

        try {
            executor.validate(params);

            ToolContext context = ToolContext.builder()
                    .taskId(taskId)
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getTaskDir(toolCode, taskId))
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
                            log.warn("发送进度失败, taskId: {}", taskId, e);
                        }
                    })
                    .logCallback(logEntry -> {
                        // 添加到日志列表
                        logs.add(logEntry);

                        // 保存到数据库
                        task.setProcessLogs(new ArrayList<>(logs));
                        taskRepository.save(task);

                        // 推送到前端
                        try {
                            if (emitter != null) {
                                emitter.send(SseEmitter.event()
                                        .name("log")
                                        .data(logEntry));
                            }
                        } catch (IOException e) {
                            log.warn("发送日志失败, taskId: {}", taskId, e);
                        }
                    })
                    .build();

            // 执行前记录日志
            context.log("开始处理任务，工具: " + toolCode);
            context.log("输入文件数量: " + inputFiles.size());
            for (Path file : inputFiles) {
                context.log("输入文件: " + file.getFileName());
            }

            R result = executor.execute(params, context);

            // 如果结果是文件处理响应，保存输出文件信息
            if (result instanceof dev.qiuyun.qiuyuntoolbackend.payload.response.FileProcessResponse) {
                dev.qiuyun.qiuyuntoolbackend.payload.response.FileProcessResponse<?> fileResult =
                        (dev.qiuyun.qiuyuntoolbackend.payload.response.FileProcessResponse<?>) result;
                if (fileResult.getDownloadUrl() != null) {
                    // 从下载URL中提取文件名
                    String downloadUrl = fileResult.getDownloadUrl();
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
                    task.setOutputFileName(fileName);
                    task.setOutputFilePath("output/" + fileName);
                }
            }

            // 执行后记录日志
            context.logSuccess("任务处理完成");

            task.setStatus(dev.qiuyun.qiuyuntoolbackend.enums.TaskStatus.COMPLETED);
            task.setProgress(100);
            task.setOutputResult(result);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);

            if (emitter != null) {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(ToolProgress.complete("处理完成", result)));
                emitter.complete();
            }

            log.info("文件处理任务执行成功, taskId: {}, 输出文件: {}", taskId, task.getOutputFileName());

        } catch (Exception e) {
            log.error("文件处理任务执行失败, taskId: {}, 任务目录: {}", taskId,
                    fileStorageService.getTaskDirAbsolutePath(toolCode, taskId), e);

            // 记录错误日志
            logs.add(new ProcessLogEntry("任务执行失败: " + e.getMessage(), "ERROR", LocalDateTime.now()));
            task.setProcessLogs(logs);

            handleTaskFailure(task, e.getMessage());
        }
    }

    /**
     * 执行异步任务（通用）
     * @param task 任务实体
     * @param executor 工具执行器
     * @param params 工具参数
     * @param inputFiles 输入文件列表
     * @param <T> 参数类型
     * @param <R> 结果类型
     */
    @Async
    public <T, R> void executeAsync(ToolTask task, ToolExecutor<T, R> executor, T params, List<Path> inputFiles) {
        String taskId = task.getTaskId();
        String toolCode = task.getToolCode();
        SseEmitter emitter = progressEmitters.get(taskId);

        // 初始化日志列表
        List<ProcessLogEntry> logs = new ArrayList<>();

        try {
            executor.validate(params);

            ToolContext context = ToolContext.builder()
                    .taskId(taskId)
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getTaskDir(toolCode, taskId))
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
                            log.warn("发送进度失败, taskId: {}", taskId, e);
                        }
                    })
                    .logCallback(logEntry -> {
                        // 添加到日志列表
                        logs.add(logEntry);

                        // 保存到数据库
                        task.setProcessLogs(new ArrayList<>(logs));
                        taskRepository.save(task);

                        // 推送到前端
                        try {
                            if (emitter != null) {
                                emitter.send(SseEmitter.event()
                                        .name("log")
                                        .data(logEntry));
                            }
                        } catch (IOException e) {
                            log.warn("发送日志失败, taskId: {}", taskId, e);
                        }
                    })
                    .build();

            // 执行前记录日志
            context.log("开始处理任务，工具: " + toolCode);

            R result = executor.execute(params, context);

            // 执行后记录日志
            context.logSuccess("任务处理完成");

            task.setStatus(dev.qiuyun.qiuyuntoolbackend.enums.TaskStatus.COMPLETED);
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

            log.info("异步任务执行成功, taskId: {}", taskId);

        } catch (Exception e) {
            log.error("异步任务执行失败, taskId: {}, 任务目录: {}", taskId,
                    fileStorageService.getTaskDirAbsolutePath(toolCode, taskId), e);

            // 记录错误日志
            logs.add(new ProcessLogEntry("任务执行失败: " + e.getMessage(), "ERROR", LocalDateTime.now()));
            task.setProcessLogs(logs);

            handleTaskFailure(task, e.getMessage());
        }
    }

    /**
     * 处理任务失败
     * @param task 任务实体
     * @param errorMessage 错误信息
     */
    private void handleTaskFailure(ToolTask task, String errorMessage) {
        String taskId = task.getTaskId();
        task.setStatus(dev.qiuyun.qiuyuntoolbackend.enums.TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        taskRepository.save(task);

        SseEmitter emitter = progressEmitters.get(taskId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(ToolProgress.error(errorMessage)));
                emitter.complete();
            } catch (IOException e) {
                log.warn("发送错误信息失败, taskId: {}", taskId, e);
            }
        }

        log.error("任务执行失败, taskId: {}, 错误: {}", taskId, errorMessage);
    }
}
