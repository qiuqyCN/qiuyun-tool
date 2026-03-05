package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {

    PENDING("pending", "等待中"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败");

    private final String code;
    private final String description;

    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return PENDING;
        }
        for (TaskStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        return PENDING;
    }
}
