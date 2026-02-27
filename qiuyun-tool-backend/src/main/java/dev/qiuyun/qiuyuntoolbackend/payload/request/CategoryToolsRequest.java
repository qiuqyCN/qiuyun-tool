package dev.qiuyun.qiuyuntoolbackend.payload.request;

import dev.qiuyun.qiuyuntoolbackend.enums.PriceMode;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolSortType;
import lombok.Data;

/**
 * 分类工具列表请求
 */
@Data
public class CategoryToolsRequest {

    /**
     * 排序方式
     */
    private ToolSortType sort = ToolSortType.VISITS;

    /**
     * 价格模式
     */
    private PriceMode price = PriceMode.ALL;
}
