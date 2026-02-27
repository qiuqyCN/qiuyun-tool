package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页数据响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeDataResponse {

    private List<CategoryResponse> categories;
    private List<ToolResponse> hotTools;
    private List<ToolResponse> newTools;
    private List<CategoryToolsResponse> categoryTools;

    /**
     * 分类工具列表
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryToolsResponse {
        private String categoryCode;
        private String categoryName;
        private List<ToolResponse> tools;
    }
}
