package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

@Getter
public enum ToolType {

    INSTANT("instant", "即时处理"),
    FILE_PROCESS("file_process", "文件处理"),
    ASYNC("async", "异步任务");

    private final String code;
    private final String description;

    ToolType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ToolType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return INSTANT;
        }
        for (ToolType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return INSTANT;
    }
}
