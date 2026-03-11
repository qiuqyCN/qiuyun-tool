package dev.qiuyun.qiuyuntoolbackend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 编解码操作类型枚举
 * 用于 Base64、URL 等编解码工具的操作类型
 */
@Getter
public enum OperationType {

    /**
     * 编码操作
     */
    ENCODE("encode", "编码"),

    /**
     * 解码操作
     */
    DECODE("decode", "解码");

    private final String code;
    private final String label;

    OperationType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 操作代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果 code 无效
     */
    public static OperationType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的操作类型: " + code));
    }

    /**
     * 根据 code 获取枚举（安全版本）
     *
     * @param code 操作代码
     * @return Optional 包装的枚举值
     */
    public static Optional<OperationType> fromCodeSafe(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * 判断 code 是否有效
     *
     * @param code 操作代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCodeSafe(code).isPresent();
    }
}
