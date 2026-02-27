package dev.qiuyun.qiuyuntoolbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类创建请求参数
 */
@Data
public class CategoryCreateRequest {

    @NotBlank(message = "分类编码不能为空")
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "分类编码只能包含小写字母、数字、下划线和连字符")
    @Size(min = 2, max = 50, message = "分类编码长度必须在2-50个字符之间")
    private String code;

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 50, message = "分类名称长度必须在2-50个字符之间")
    private String name;

    @Size(max = 50, message = "图标长度不能超过50个字符")
    private String icon;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    private Integer sortOrder = 0;
}
