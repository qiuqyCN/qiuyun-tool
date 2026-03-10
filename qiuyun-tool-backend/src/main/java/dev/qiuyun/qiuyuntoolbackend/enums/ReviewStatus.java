package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

/**
 * 评论状态枚举
 */
@Getter
public enum ReviewStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "正常");

    private final int code;
    private final String desc;

    ReviewStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReviewStatus of(Integer code) {
        if (code == null) {
            return ENABLED;
        }
        for (ReviewStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ENABLED;
    }

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }
}
