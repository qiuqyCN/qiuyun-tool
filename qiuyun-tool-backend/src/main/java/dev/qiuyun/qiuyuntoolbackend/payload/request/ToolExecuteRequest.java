package dev.qiuyun.qiuyuntoolbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行请求
 * @param <T> 工具特定的请求参数类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolExecuteRequest<T> {

    @NotBlank(message = "工具ID不能为空")
    private String toolCode;

    /**
     * 工具特定的请求参数
     * 具体工具可以定义自己的参数类
     */
    private T params;
}
