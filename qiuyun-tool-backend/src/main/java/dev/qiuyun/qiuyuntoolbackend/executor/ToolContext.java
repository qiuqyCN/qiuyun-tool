package dev.qiuyun.qiuyuntoolbackend.executor;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolProgress;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * 工具执行上下文
 * 包含工具执行过程中需要的所有上下文信息
 */
@Getter
@Builder
public class ToolContext {

    /**
     * 任务ID，唯一标识一次工具执行
     */
    private String taskId;

    /**
     * 用户ID，执行工具的用户
     */
    private Long userId;

    /**
     * 临时目录，工具可以在此目录读写临时文件
     */
    private Path tempDir;

    /**
     * 进度回调函数，用于向客户端发送执行进度
     */
    private Consumer<ToolProgress> progressCallback;

    /**
     * 输入文件列表，用户上传的所有文件
     */
    private List<Path> inputFiles;

    /**
     * 更新执行进度（不带附加数据）
     * @param percent 进度百分比（0-100）
     * @param message 进度消息
     */
    public void updateProgress(Integer percent, String message) {
        if (progressCallback != null) {
            progressCallback.accept(ToolProgress.of(percent, message));
        }
    }

    /**
     * 更新执行进度（带附加数据）
     * @param percent 进度百分比（0-100）
     * @param message 进度消息
     * @param data 附加数据
     */
    public void updateProgress(Integer percent, String message, Object data) {
        if (progressCallback != null) {
            progressCallback.accept(ToolProgress.of(percent, message, data));
        }
    }

    /**
     * 完成进度
     * @param message 完成消息
     */
    public void completeProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(ToolProgress.complete(message));
        }
    }
}
