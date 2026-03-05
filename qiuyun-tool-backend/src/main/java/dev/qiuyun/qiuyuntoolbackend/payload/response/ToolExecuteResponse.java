package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行响应
 * @param <T> 工具特定的响应结果类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolExecuteResponse<T> {

    private String taskId;

    private String status;

    /**
     * 工具特定的响应结果
     * 具体工具可以定义自己的响应类，如：
     * - 即时工具：直接返回结果数据
     * - 文件处理工具：继承 FileProcessResponse
     * - 异步任务工具：继承 AsyncTaskResponse
     */
    private T result;

    private Integer progress;

    private String message;
}
