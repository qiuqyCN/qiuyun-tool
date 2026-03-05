package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 文件处理类型工具的通用响应父类
 * 所有需要处理文件的工具响应类应继承此类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FileProcessResponse<T> {

    /**
     * 文件下载链接
     */
    private String downloadUrl;

    /**
     * 处理结果数据
     */
    private T data;
}
