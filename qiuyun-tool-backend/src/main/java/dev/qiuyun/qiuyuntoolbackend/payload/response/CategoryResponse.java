package dev.qiuyun.qiuyuntoolbackend.payload.response;

import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String code;
    private String name;
    private String icon;
    private String description;

    /**
     * 从 Category 实体创建响应对象
     */
    public static CategoryResponse from(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .icon(category.getIcon())
                .description(category.getDescription())
                .build();
    }
}
