package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {

    DISABLED(0, "禁用"),
    ENABLED(1, "正常");

    private final int code;
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus of(Integer code) {
        if (code == null) {
            return ENABLED;
        }
        for (UserStatus status : values()) {
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
