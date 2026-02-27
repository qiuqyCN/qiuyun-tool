package dev.qiuyun.qiuyuntoolbackend.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工具搜索请求参数
 */
@Data
public class ToolSearchRequest {

    @Size(max = 100, message = "搜索关键词长度不能超过100个字符")
    private String keyword;

    @Pattern(regexp = "^(hot|new|all)?$", message = "排序方式只能是 hot、new 或 all")
    private String sortBy = "all";

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer size = 20;
}
