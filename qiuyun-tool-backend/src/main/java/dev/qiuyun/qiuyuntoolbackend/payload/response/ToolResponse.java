package dev.qiuyun.qiuyuntoolbackend.payload.response;

import dev.qiuyun.qiuyuntoolbackend.entity.Tag;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private String icon;
    private String iconColor;
    private String iconBgColor;
    private Boolean isVip;
    private Boolean isHot;
    private String priceMode;
    private Long visits;
    private Long viewCount;
    private Long usageCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer favoriteCount;
    private String instructions;
    private List<String> tags;
    private LocalDateTime createdAt;

    /**
     * 从 Tool 实体创建响应对象（完整版本）
     */
    public static ToolResponse from(Tool tool) {
        if (tool == null) {
            return null;
        }
        return ToolResponse.builder()
                .id(tool.getId())
                .code(tool.getCode())
                .name(tool.getName())
                .description(tool.getDescription())
                .category(tool.getCategory() != null ? tool.getCategory().getCode() : null)
                .icon(tool.getIcon())
                .iconColor(tool.getIconColor())
                .iconBgColor(tool.getIconBgColor())
                .isVip(tool.getIsVip())
                .isHot(tool.getIsHot())
                .priceMode(tool.getPriceMode())
                .visits(tool.getVisitsCount())
                .viewCount(tool.getViewCount())
                .usageCount(tool.getUsageCount())
                .rating(tool.getRating())
                .reviewCount(tool.getReviewCount())
                .favoriteCount(tool.getFavoriteCount())
                .instructions(tool.getInstructions())
                .tags(tool.getTags() != null ? tool.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()) : null)
                .createdAt(tool.getCreatedAt())
                .build();
    }

    /**
     * 从 Tool 实体创建响应对象（简单版本，用于列表展示）
     */
    public static ToolResponse simpleFrom(Tool tool) {
        if (tool == null) {
            return null;
        }
        return ToolResponse.builder()
                .id(tool.getId())
                .code(tool.getCode())
                .name(tool.getName())
                .description(tool.getDescription())
                .category(tool.getCategory() != null ? tool.getCategory().getCode() : null)
                .icon(tool.getIcon())
                .isVip(tool.getIsVip())
                .isHot(tool.getIsHot())
                .visits(tool.getVisitsCount())
                .rating(tool.getRating())
                .reviewCount(tool.getReviewCount())
                .tags(tool.getTags() != null ? tool.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
