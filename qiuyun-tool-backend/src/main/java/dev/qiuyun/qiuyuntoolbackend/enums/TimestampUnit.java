package dev.qiuyun.qiuyuntoolbackend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 时间戳单位枚举
 * 用于时间戳转换工具的单位类型
 */
@Getter
public enum TimestampUnit {

    /**
     * 秒级时间戳
     */
    SECONDS("seconds", "秒", 1L),

    /**
     * 毫秒级时间戳
     */
    MILLISECONDS("milliseconds", "毫秒", 1000L);

    private final String code;
    private final String label;
    private final long multiplier;

    TimestampUnit(String code, String label, long multiplier) {
        this.code = code;
        this.label = label;
        this.multiplier = multiplier;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 将毫秒转换为当前单位的值
     *
     * @param millis 毫秒值
     * @return 转换后的值
     */
    public long fromMillis(long millis) {
        return millis / multiplier;
    }

    /**
     * 将当前单位的值转换为毫秒
     *
     * @param value 当前单位的值
     * @return 毫秒值
     */
    public long toMillis(long value) {
        return value * multiplier;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 单位代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果 code 无效
     */
    public static TimestampUnit fromCode(String code) {
        return Arrays.stream(values())
                .filter(unit -> unit.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的时间戳单位: " + code));
    }

    /**
     * 根据 code 获取枚举（安全版本）
     *
     * @param code 单位代码
     * @return Optional 包装的枚举值
     */
    public static Optional<TimestampUnit> fromCodeSafe(String code) {
        return Arrays.stream(values())
                .filter(unit -> unit.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * 判断 code 是否有效
     *
     * @param code 单位代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCodeSafe(code).isPresent();
    }
}
