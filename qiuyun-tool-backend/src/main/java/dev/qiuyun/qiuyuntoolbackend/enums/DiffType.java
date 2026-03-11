package dev.qiuyun.qiuyuntoolbackend.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 差异类型枚举
 * 用于文本对比工具的差异化类型标识
 */
@Getter
public enum DiffType {

    /**
     * 新增
     */
    ADDED("added", "新增", "success"),

    /**
     * 删除
     */
    REMOVED("removed", "删除", "danger"),

    /**
     * 修改
     */
    MODIFIED("modified", "修改", "warning"),

    /**
     * 未变更
     */
    UNCHANGED("unchanged", "未变更", "default");

    private final String code;
    private final String label;
    private final String style;

    DiffType(String code, String label, String style) {
        this.code = code;
        this.label = label;
        this.style = style;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 类型代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果 code 无效
     */
    public static DiffType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的差异类型: " + code));
    }

    /**
     * 根据 code 获取枚举（安全版本）
     *
     * @param code 类型代码
     * @return Optional 包装的枚举值
     */
    public static Optional<DiffType> fromCodeSafe(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * 判断 code 是否有效
     *
     * @param code 类型代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCodeSafe(code).isPresent();
    }

    /**
     * 是否为新增类型
     *
     * @return 是否为新增
     */
    public boolean isAdded() {
        return this == ADDED;
    }

    /**
     * 是否为删除类型
     *
     * @return 是否为删除
     */
    public boolean isRemoved() {
        return this == REMOVED;
    }

    /**
     * 是否为修改类型
     *
     * @return 是否为修改
     */
    public boolean isModified() {
        return this == MODIFIED;
    }

    /**
     * 是否为未变更类型
     *
     * @return 是否为未变更
     */
    public boolean isUnchanged() {
        return this == UNCHANGED;
    }

    /**
     * 是否为变更类型（新增、删除、修改）
     *
     * @return 是否发生变更
     */
    public boolean isChanged() {
        return this != UNCHANGED;
    }
}
