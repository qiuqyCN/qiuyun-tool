package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolProgress {

    private Integer percent;

    private String message;

    private Object data;

    private Boolean completed;

    public static ToolProgress of(Integer percent, String message) {
        return ToolProgress.builder()
                .percent(percent)
                .message(message)
                .completed(false)
                .build();
    }

    public static ToolProgress of(Integer percent, String message, Object data) {
        return ToolProgress.builder()
                .percent(percent)
                .message(message)
                .data(data)
                .completed(false)
                .build();
    }

    public static ToolProgress complete() {
        return ToolProgress.builder()
                .percent(100)
                .message("处理完成")
                .completed(true)
                .build();
    }

    public static ToolProgress complete(String message) {
        return ToolProgress.builder()
                .percent(100)
                .message(message)
                .completed(true)
                .build();
    }

    public static ToolProgress error(String message) {
        return ToolProgress.builder()
                .percent(0)
                .message(message)
                .completed(true)
                .build();
    }
}
