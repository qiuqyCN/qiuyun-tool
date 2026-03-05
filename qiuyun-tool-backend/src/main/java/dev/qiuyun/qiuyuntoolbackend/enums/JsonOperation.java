package dev.qiuyun.qiuyuntoolbackend.enums;

import lombok.Getter;

/**
 * JSON格式化操作类型枚举
 */
@Getter
public enum JsonOperation {
    FORMAT("format", "格式化"),
    COMPRESS("compress", "压缩"),
    ESCAPE("escape", "转义"),
    UNESCAPE("unescape", "去转义");

    private final String code;
    private final String label;

    JsonOperation(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 根据code获取枚举
     */
    public static JsonOperation fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (JsonOperation operation : values()) {
            if (operation.code.equalsIgnoreCase(code.trim())) {
                return operation;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的操作类型
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
