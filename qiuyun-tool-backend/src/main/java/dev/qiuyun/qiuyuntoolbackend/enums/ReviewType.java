package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

/**
 * 评论类型枚举
 */
@Getter
public enum ReviewType {

    REVIEW(0, "评论"),
    REPLY(1, "回复");

    private final int code;
    private final String desc;

    ReviewType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReviewType of(Integer code) {
        if (code == null) {
            return REVIEW;
        }
        for (ReviewType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return REVIEW;
    }

    public boolean isReview() {
        return this == REVIEW;
    }

    public boolean isReply() {
        return this == REPLY;
    }
}
