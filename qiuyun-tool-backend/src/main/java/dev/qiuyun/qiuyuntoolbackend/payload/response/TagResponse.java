package dev.qiuyun.qiuyuntoolbackend.payload.response;

import dev.qiuyun.qiuyuntoolbackend.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResponse {

    private Long id;
    private String name;
    private String description;

    /**
     * 从 Tag 实体创建响应对象
     */
    public static TagResponse from(Tag tag) {
        if (tag == null) {
            return null;
        }
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .build();
    }
}
