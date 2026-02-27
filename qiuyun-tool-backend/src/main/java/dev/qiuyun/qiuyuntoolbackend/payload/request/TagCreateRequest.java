package dev.qiuyun.qiuyuntoolbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 标签创建请求参数
 */
@Data
public class TagCreateRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(min = 1, max = 50, message = "标签名称长度必须在1-50个字符之间")
    private String name;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    private Boolean isHot = false;
}
