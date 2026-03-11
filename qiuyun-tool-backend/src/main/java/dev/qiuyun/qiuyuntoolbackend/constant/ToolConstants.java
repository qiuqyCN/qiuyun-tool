package dev.qiuyun.qiuyuntoolbackend.constant;

/**
 * 工具常量类
 * 定义工具模块通用的常量
 */
public final class ToolConstants {

    private ToolConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 字符集常量 ====================

    /**
     * 默认字符集
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 支持的字符集列表
     */
    public static final String[] COMMON_CHARSETS = {"UTF-8", "GBK", "ISO-8859-1", "ASCII"};

    // ==================== 进制常量 ====================

    /**
     * 最小进制
     */
    public static final int MIN_RADIX = 2;

    /**
     * 最大进制
     */
    public static final int MAX_RADIX = 36;

    /**
     * 常用进制
     */
    public static final int[] COMMON_RADIXES = {2, 8, 10, 16};

    // ==================== 随机数生成常量 ====================

    /**
     * 默认生成数量
     */
    public static final int DEFAULT_RANDOM_COUNT = 1;

    /**
     * 最大生成数量
     */
    public static final int MAX_RANDOM_COUNT = 100;

    /**
     * 默认密码长度
     */
    public static final int DEFAULT_PASSWORD_LENGTH = 16;

    /**
     * 最大密码长度
     */
    public static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * 默认字符串长度
     */
    public static final int DEFAULT_STRING_LENGTH = 16;

    /**
     * 最大字符串长度
     */
    public static final int MAX_STRING_LENGTH = 1000;

    // ==================== MD5 常量 ====================

    /**
     * MD5 32位
     */
    public static final int MD5_32BIT = 32;

    /**
     * MD5 16位
     */
    public static final int MD5_16BIT = 16;

    // ==================== 枚举类引用说明 ====================
    // 以下常量已抽取为枚举类，请使用对应的枚举：
    // - 编码/解码操作：dev.qiuyun.qiuyuntoolbackend.enums.OperationType
    // - 时间戳单位：dev.qiuyun.qiuyuntoolbackend.enums.TimestampUnit
    // - 文本对比模式：dev.qiuyun.qiuyuntoolbackend.enums.CompareMode
    // - 差异类型：dev.qiuyun.qiuyuntoolbackend.enums.DiffType
}
