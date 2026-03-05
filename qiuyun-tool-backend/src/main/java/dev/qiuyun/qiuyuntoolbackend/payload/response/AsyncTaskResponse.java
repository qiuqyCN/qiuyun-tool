package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 异步任务类型工具的通用响应父类
 * 所有异步执行的工具响应类应继承此类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AsyncTaskResponse<T> {

    /**
     * 任务预计完成时间
     */
    private LocalDateTime estimatedCompleteTime;

    /**
     * 任务结果查询链接
     */
    private String resultUrl;

    /**
     * 处理结果数据
     */
    private T data;
}
