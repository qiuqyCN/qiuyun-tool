package dev.qiuyun.qiuyuntoolbackend.executor.common;

import lombok.Data;

/**
 * 工具响应基类
 * 所有工具响应类都应继承此类
 */
@Data
public abstract class BaseToolResponse {
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息
     */
    private String errorMessage;
}
