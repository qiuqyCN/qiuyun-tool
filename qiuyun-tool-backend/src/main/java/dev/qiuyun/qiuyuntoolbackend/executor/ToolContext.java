package dev.qiuyun.qiuyuntoolbackend.executor;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolProgress;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.function.Consumer;

@Getter
@Builder
public class ToolContext {

    private String taskId;

    private Long userId;

    private Path tempDir;

    private Consumer<ToolProgress> progressCallback;

    public void updateProgress(Integer percent, String message) {
        if (progressCallback != null) {
            progressCallback.accept(ToolProgress.of(percent, message));
        }
    }

    public void updateProgress(Integer percent, String message, Object data) {
        if (progressCallback != null) {
            progressCallback.accept(ToolProgress.of(percent, message, data));
        }
    }

    public void completeProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(ToolProgress.complete(message));
        }
    }
}
