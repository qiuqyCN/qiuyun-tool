package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

/**
 * 价格模式枚举
 */
@Getter
public enum PriceMode {

    /**
     * 全部
     */
    ALL("all", "全部"),

    /**
     * 免费
     */
    FREE("free", "免费"),

    /**
     * VIP
     */
    VIP("vip", "VIP");

    private final String code;
    private final String description;

    PriceMode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static PriceMode fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ALL;
        }
        for (PriceMode mode : values()) {
            if (mode.code.equalsIgnoreCase(code.trim())) {
                return mode;
            }
        }
        return ALL;
    }
}
