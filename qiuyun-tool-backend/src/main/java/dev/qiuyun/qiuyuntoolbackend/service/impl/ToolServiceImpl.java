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
import dev.qiuyun.qiuyuntoolbackend.service.AsyncTaskService;
import dev.qiuyun.qiuyuntoolbackend.service.FileStorageService;
import dev.qiuyun.qiuyuntoolbackend.service.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final AsyncTaskService asyncTaskService;
    private final ObjectMapper objectMapper;

    /**
     * 获取文件输入流
     * @param filePath 文件路径
     * @return 文件输入流
     */
    @Override
    public InputStream getFileStream(Path filePath) {
        return fileStorageService.getFileStream(filePath);
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
        log.info("开始执行任务, toolCode: {}, taskId: {}, userId: {}", toolCode, taskId, userId);

        // 保存输入文件到任务目录
        List<Path> inputFiles = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            try {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        Path filePath = fileStorageService.saveTaskInputFile(
                                file.getInputStream(),
                                file.getOriginalFilename(),
                                toolCode,
                                taskId
                        );
                        inputFiles.add(filePath);
                        log.info("任务输入文件保存成功, taskId: {}, 文件: {}", taskId, filePath.toAbsolutePath());
                    }
                }
            } catch (IOException e) {
                log.error("保存任务输入文件失败, taskId: {}, toolCode: {}", taskId, toolCode, e);
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
        log.info("任务已创建, taskId: {}, toolCode: {}", taskId, toolCode);

        ToolType toolType = executor.getToolType();

        if (toolType == ToolType.INSTANT) {
            return executeInstant(task, executor, convertedParams, inputFiles);
        } else if (toolType == ToolType.FILE_PROCESS) {
            // FILE_PROCESS 类型改为异步执行
            return executeFileProcessAsync(task, executor, convertedParams, inputFiles);
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
        String taskId = task.getTaskId();
        String toolCode = task.getToolCode();
        log.info("开始同步执行任务, taskId: {}, toolCode: {}", taskId, toolCode);

        try {
            task.setStatus(TaskStatus.PROCESSING);
            taskRepository.save(task);

            executor.validate(params);

            ToolContext context = ToolContext.builder()
                    .taskId(taskId)
                    .userId(task.getUserId())
                    .tempDir(fileStorageService.getTaskDir(toolCode, taskId))
                    .inputFiles(inputFiles)
                    .build();

            log.info("执行任务逻辑, taskId: {}, 任务目录: {}", taskId, context.getTempDir().toAbsolutePath());

            R result = executor.execute(params, context);

            task.setStatus(TaskStatus.COMPLETED);
            task.setProgress(100);
            task.setOutputResult(result);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);

            log.info("任务执行成功, taskId: {}", taskId);

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
            log.error("任务执行失败, taskId: {}, 错误: {}", taskId, e.getMessage());
            throw e;
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("执行失败: " + e.getMessage());
            taskRepository.save(task);
            log.error("任务执行失败, taskId: {}, 任务目录: {}", taskId,
                    fileStorageService.getTaskDirAbsolutePath(toolCode, taskId), e);
            throw new BusinessException("执行失败: " + e.getMessage());
        }
    }

    /**
     * 执行文件处理类型工具（异步执行，立即返回任务ID）
     * @param task 任务实体
     * @param executor 工具执行器
     * @param params 工具参数
     * @param inputFiles 输入文件列表
     * @param <T> 参数类型
     * @param <R> 结果类型
     * @return 工具执行响应（任务已提交）
     */
    private <T, R> ToolExecuteResponse<R> executeFileProcessAsync(ToolTask task, ToolExecutor<T, R> executor, T params, List<Path> inputFiles) {
        String taskId = task.getTaskId();
        String toolCode = task.getToolCode();
        log.info("开始异步执行文件处理任务, taskId: {}, toolCode: {}", taskId, toolCode);

        task.setStatus(TaskStatus.PROCESSING);
        taskRepository.save(task);

        // 创建SSE发射器
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1小时超时
        emitter.onCompletion(() -> asyncTaskService.removeEmitter(taskId));
        emitter.onTimeout(() -> asyncTaskService.removeEmitter(taskId));
        emitter.onError(e -> asyncTaskService.removeEmitter(taskId));

        // 注册发射器到异步任务服务
        asyncTaskService.registerEmitter(taskId, emitter);

        // 调用异步服务执行（通过另一个Service调用，@Async才会生效）
        asyncTaskService.executeFileProcess(task, executor, params, inputFiles);

        log.info("文件处理任务已提交, taskId: {}, 任务目录: {}", taskId,
                fileStorageService.getTaskDirAbsolutePath(toolCode, taskId));

        return ToolExecuteResponse.<R>builder()
                .taskId(taskId)
                .status(TaskStatus.PROCESSING.getCode())
                .progress(0)
                .message("任务已提交，正在处理中...")
                .build();
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
        String taskId = task.getTaskId();
        String toolCode = task.getToolCode();
        log.info("开始执行异步任务, taskId: {}, toolCode: {}", taskId, toolCode);

        task.setStatus(TaskStatus.PROCESSING);
        taskRepository.save(task);

        // 创建SSE发射器
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1小时超时
        emitter.onCompletion(() -> asyncTaskService.removeEmitter(taskId));
        emitter.onTimeout(() -> asyncTaskService.removeEmitter(taskId));
        emitter.onError(e -> asyncTaskService.removeEmitter(taskId));

        // 注册发射器到异步任务服务
        asyncTaskService.registerEmitter(taskId, emitter);

        // 调用异步服务执行
        asyncTaskService.executeAsync(task, executor, params, inputFiles);

        log.info("异步任务已提交, taskId: {}, 任务目录: {}", taskId,
                fileStorageService.getTaskDirAbsolutePath(toolCode, taskId));

        return ToolExecuteResponse.<R>builder()
                .taskId(taskId)
                .status(TaskStatus.PROCESSING.getCode())
                .progress(0)
                .message("任务已提交")
                .build();
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
                            .data(ToolProgress.complete("处理完成", task.getOutputResult())));
                } else {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(ToolProgress.error(task.getErrorMessage())));
                }
                emitter.complete();
            } catch (IOException e) {
                log.warn("发送状态失败, taskId: {}", taskId, e);
            }
            return emitter;
        }

        // 检查异步任务服务中是否已有发射器
        SseEmitter existingEmitter = asyncTaskService.getEmitter(taskId);
        if (existingEmitter != null) {
            return existingEmitter;
        }

        // 创建新的发射器
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1小时超时
        emitter.onCompletion(() -> asyncTaskService.removeEmitter(taskId));
        emitter.onTimeout(() -> asyncTaskService.removeEmitter(taskId));
        emitter.onError(e -> asyncTaskService.removeEmitter(taskId));

        asyncTaskService.registerEmitter(taskId, emitter);

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
     * 获取任务输出文件路径
     * @param taskId 任务ID
     * @return 输出文件路径
     */
    @Override
    public Path getTaskOutputFilePath(String taskId) {
        ToolTask task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new BusinessException("任务未完成");
        }

        if (task.getOutputFileName() == null || task.getOutputFilePath() == null) {
            throw new BusinessException("任务没有输出文件");
        }

        return fileStorageService.getTaskOutputFilePath(
                task.getToolCode(),
                taskId,
                task.getOutputFileName()
        );
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

            SseEmitter emitter = asyncTaskService.getEmitter(taskId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(ToolProgress.error("用户取消")));
                    emitter.complete();
                } catch (IOException e) {
                    log.warn("发送取消信息失败, taskId: {}", taskId, e);
                }
            }

            log.info("任务已取消, taskId: {}", taskId);
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
