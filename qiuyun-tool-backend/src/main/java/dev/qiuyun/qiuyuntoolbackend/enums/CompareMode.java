package dev.qiuyun.qiuyuntoolbackend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 文本对比模式枚举
 * 用于文本对比工具的对比模式
 */
@Getter
public enum CompareMode {

    /**
     * 行对比模式
     */
    LINE("line", "行对比"),

    /**
     * 字符对比模式
     */
    CHAR("char", "字符对比");

    private final String code;
    private final String label;

    CompareMode(String code, String label) {
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
     * @param code 模式代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果 code 无效
     */
    public static CompareMode fromCode(String code) {
        return Arrays.stream(values())
                .filter(mode -> mode.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的对比模式: " + code));
    }

    /**
     * 根据 code 获取枚举（安全版本）
     *
     * @param code 模式代码
     * @return Optional 包装的枚举值
     */
    public static Optional<CompareMode> fromCodeSafe(String code) {
        return Arrays.stream(values())
                .filter(mode -> mode.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * 判断 code 是否有效
     *
     * @param code 模式代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCodeSafe(code).isPresent();
    }

    /**
     * 是否为行对比模式
     *
     * @return 是否为行对比
     */
    public boolean isLine() {
        return this == LINE;
    }

    /**
     * 是否为字符对比模式
     *
     * @return 是否为字符对比
     */
    public boolean isChar() {
        return this == CHAR;
    }
}
