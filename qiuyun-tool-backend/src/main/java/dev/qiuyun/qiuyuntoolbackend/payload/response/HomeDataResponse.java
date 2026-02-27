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
    private List<TagResponse> hotTags;
}
