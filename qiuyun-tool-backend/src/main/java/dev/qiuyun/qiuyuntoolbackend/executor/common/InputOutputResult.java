package dev.qiuyun.qiuyuntoolbackend.executor.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 输入输出结果通用类
 * 用于编码/解码、加密等工具的结果存储
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputOutputResult {
    /**
     * 输入内容
     */
    private String input;
    /**
     * 输出内容
     */
    private String output;
}
