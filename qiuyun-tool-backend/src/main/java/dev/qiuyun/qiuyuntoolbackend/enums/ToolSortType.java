package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

/**
 * 工具排序方式枚举
 */
@Getter
public enum ToolSortType {

    /**
     * 按访问量排序（降序）
     */
    VISITS("visits", "访问量"),

    /**
     * 按评分排序（降序）
     */
    RATING("rating", "评分"),

    /**
     * 按最新创建排序（降序）
     */
    NEWEST("newest", "最新"),

    /**
     * 按名称排序（A-Z）
     */
    NAME("name", "名称");

    private final String code;
    private final String description;

    ToolSortType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ToolSortType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return VISITS;
        }
        for (ToolSortType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return VISITS;
    }
}
