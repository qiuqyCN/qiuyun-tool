package dev.qiuyun.qiuyuntoolbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 文件处理类型工具的通用请求父类
 * 所有需要处理文件的工具请求类应继承此类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FileProcessRequest {

    @NotBlank(message = "文件ID不能为空")
    private String fileId;
}
