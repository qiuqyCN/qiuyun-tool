package dev.qiuyun.qiuyuntoolbackend.executor.text;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 汉字转拼音工具执行器
 * 支持：带声调拼音、不带声调拼音、首字母提取、多音字处理等
 */
@Slf4j
@Component
public class ChineseToPinyinExecutor extends AbstractToolExecutor<ChineseToPinyinExecutor.ChineseToPinyinRequest, ChineseToPinyinExecutor.ChineseToPinyinResponse> {

    // 中文字符正则表达式
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    // 英文字母正则表达式
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]");
    // 数字正则表达式
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");

    @Override
    public String getToolCode() {
        return "chinese-to-pinyin";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ChineseToPinyinRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getText(), "输入文本");
        validateEnum(request.getToneType(), "声调类型", List.of("with_tone", "without_tone", "tone_number"));
        validateEnum(request.getCaseType(), "大小写类型", List.of("lowercase", "uppercase", "capitalize"));
        validateEnum(request.getSeparator(), "分隔符", List.of("space", "none", "dash", "underline"));
    }

    @Override
    protected ChineseToPinyinResponse doExecute(ChineseToPinyinRequest request, ToolContext context) throws Exception {
        String text = request.getText().trim();

        ChineseToPinyinResponse response = new ChineseToPinyinResponse();
        response.setSuccess(true);
        response.setOriginalText(text);

        // 统计信息
        TextStats stats = analyzeText(text);
        response.setStats(stats);

        // 转换为拼音
        String pinyin = convertToPinyin(text, request);
        response.setPinyin(pinyin);

        // 提取首字母
        if (request.isExtractInitials()) {
            String initials = extractInitials(text, request.getSeparator());
            response.setInitials(initials);
        }

        // 多音字检测
        if (request.isDetectPolyphone()) {
            List<PolyphoneInfo> polyphones = detectPolyphones(text);
            response.setPolyphones(polyphones);
        }

        // 逐字转换详情
        if (request.isShowDetails()) {
            List<CharDetail> details = convertWithDetails(text, request);
            response.setDetails(details);
        }

        return response;
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "汉字转拼音",
                "description", "将汉字转换为拼音，支持声调、首字母提取、多音字检测",
                "features", new String[]{"toneOptions", "initials", "polyphone", "details"}
        );
    }

    /**
     * 分析文本统计信息
     */
    private TextStats analyzeText(String text) {
        TextStats stats = new TextStats();

        int chineseCount = 0;
        int englishCount = 0;
        int numberCount = 0;
        int otherCount = 0;

        for (char c : text.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                chineseCount++;
            } else if (ENGLISH_PATTERN.matcher(String.valueOf(c)).matches()) {
                englishCount++;
            } else if (NUMBER_PATTERN.matcher(String.valueOf(c)).matches()) {
                numberCount++;
            } else {
                otherCount++;
            }
        }

        stats.setTotalChars(text.length());
        stats.setChineseChars(chineseCount);
        stats.setEnglishChars(englishCount);
        stats.setNumbers(numberCount);
        stats.setOtherChars(otherCount);

        return stats;
    }

    /**
     * 转换文本为拼音
     */
    private String convertToPinyin(String text, ChineseToPinyinRequest request) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat format = createOutputFormat(request);
        String separator = getSeparator(request.getSeparator());
        boolean keepNonChinese = request.isKeepNonChinese();
        boolean capitalize = "capitalize".equals(request.getCaseType());

        StringBuilder result = new StringBuilder();
        boolean firstWord = true;

        for (char c : text.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    if (!firstWord) {
                        result.append(separator);
                    }
                    String pinyin = pinyinArray[0];
                    // 首字母大写处理
                    if (capitalize && !pinyin.isEmpty()) {
                        pinyin = Character.toUpperCase(pinyin.charAt(0)) + pinyin.substring(1);
                    }
                    result.append(pinyin);
                    firstWord = false;
                }
            } else if (keepNonChinese) {
                if (!firstWord && !separator.isEmpty() && !Character.isWhitespace(c)) {
                    // 非中文字符前不添加分隔符，除非是空格
                }
                result.append(c);
                if (!Character.isWhitespace(c)) {
                    firstWord = false;
                }
            }
        }

        return result.toString().trim();
    }

    /**
     * 提取首字母
     */
    private String extractInitials(String text, String separatorType) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        String separator = getSeparator(separatorType);
        StringBuilder result = new StringBuilder();
        boolean firstWord = true;

        for (char c : text.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    if (!firstWord) {
                        result.append(separator);
                    }
                    result.append(pinyinArray[0].charAt(0));
                    firstWord = false;
                }
            } else if (ENGLISH_PATTERN.matcher(String.valueOf(c)).matches()) {
                if (!firstWord) {
                    result.append(separator);
                }
                result.append(Character.toLowerCase(c));
                firstWord = false;
            }
        }

        return result.toString();
    }

    /**
     * 检测多音字
     */
    private List<PolyphoneInfo> detectPolyphones(String text) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        List<PolyphoneInfo> polyphones = new ArrayList<>();
        int position = 0;

        for (char c : text.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyinArray != null && pinyinArray.length > 1) {
                    PolyphoneInfo info = new PolyphoneInfo();
                    info.setCharacter(String.valueOf(c));
                    info.setPosition(position);
                    info.setReadings(List.of(pinyinArray));
                    polyphones.add(info);
                }
            }
            position++;
        }

        return polyphones;
    }

    /**
     * 逐字转换详情
     */
    private List<CharDetail> convertWithDetails(String text, ChineseToPinyinRequest request) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat formatWithTone = createOutputFormat(request);
        HanyuPinyinOutputFormat formatWithoutTone = new HanyuPinyinOutputFormat();
        formatWithoutTone.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        formatWithoutTone.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        formatWithoutTone.setVCharType(HanyuPinyinVCharType.WITH_V);

        List<CharDetail> details = new ArrayList<>();

        for (char c : text.toCharArray()) {
            CharDetail detail = new CharDetail();
            detail.setCharacter(String.valueOf(c));

            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                detail.setChinese(true);

                String[] withToneArray = PinyinHelper.toHanyuPinyinStringArray(c, formatWithTone);
                String[] withoutToneArray = PinyinHelper.toHanyuPinyinStringArray(c, formatWithoutTone);

                if (withToneArray != null && withToneArray.length > 0) {
                    detail.setPinyinWithTone(withToneArray[0]);
                    detail.setPinyinWithoutTone(withoutToneArray[0]);
                    detail.setInitial(withoutToneArray[0].substring(0, 1).toUpperCase());

                    // 多音字所有读音
                    if (withToneArray.length > 1) {
                        detail.setAlternativeReadings(List.of(withToneArray));
                    }
                }
            } else {
                detail.setChinese(false);
            }

            details.add(detail);
        }

        return details;
    }

    /**
     * 创建拼音输出格式
     */
    private HanyuPinyinOutputFormat createOutputFormat(ChineseToPinyinRequest request) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

        // 设置大小写
        switch (request.getCaseType()) {
            case "uppercase":
                format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
                break;
            case "capitalize":
                format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
                break;
            default:
                format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        }

        // 设置声调
        switch (request.getToneType()) {
            case "with_tone":
                format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
                // 带声调符号时，使用 Unicode 形式的 ü，以便正确显示带声调的 ü
                format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
                break;
            case "tone_number":
                format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
                format.setVCharType(HanyuPinyinVCharType.WITH_V);
                break;
            default:
                format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
                format.setVCharType(HanyuPinyinVCharType.WITH_V);
        }

        return format;
    }

    /**
     * 获取分隔符
     */
    private String getSeparator(String separatorType) {
        return switch (separatorType) {
            case "space" -> " ";
            case "dash" -> "-";
            case "underline" -> "_";
            default -> "";
        };
    }

    /**
     * 请求参数
     */
    @Data
    public static class ChineseToPinyinRequest {
        /**
         * 输入文本
         */
        private String text;

        /**
         * 声调类型: with_tone(带声调符号), without_tone(不带声调), tone_number(数字声调)
         */
        private String toneType = "without_tone";

        /**
         * 大小写类型: lowercase(小写), uppercase(大写), capitalize(首字母大写)
         */
        private String caseType = "lowercase";

        /**
         * 分隔符: space(空格), none(无), dash(连字符), underline(下划线)
         */
        private String separator = "space";

        /**
         * 是否保留非中文字符
         */
        private boolean keepNonChinese = true;

        /**
         * 是否提取首字母
         */
        private boolean extractInitials = false;

        /**
         * 是否检测多音字
         */
        private boolean detectPolyphone = false;

        /**
         * 是否显示逐字详情
         */
        private boolean showDetails = false;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ChineseToPinyinResponse extends BaseToolResponse {
        /**
         * 原始文本
         */
        private String originalText;

        /**
         * 转换后的拼音
         */
        private String pinyin;

        /**
         * 首字母
         */
        private String initials;

        /**
         * 文本统计
         */
        private TextStats stats;

        /**
         * 多音字列表
         */
        private List<PolyphoneInfo> polyphones;

        /**
         * 逐字详情
         */
        private List<CharDetail> details;
    }

    /**
     * 文本统计
     */
    @Data
    public static class TextStats {
        /**
         * 总字符数
         */
        private int totalChars;

        /**
         * 中文字符数
         */
        private int chineseChars;

        /**
         * 英文字符数
         */
        private int englishChars;

        /**
         * 数字个数
         */
        private int numbers;

        /**
         * 其他字符数
         */
        private int otherChars;
    }

    /**
     * 多音字信息
     */
    @Data
    public static class PolyphoneInfo {
        /**
         * 汉字字符
         */
        private String character;

        /**
         * 在文本中的位置
         */
        private int position;

        /**
         * 所有读音
         */
        private List<String> readings;
    }

    /**
     * 字符详情
     */
    @Data
    public static class CharDetail {
        /**
         * 字符
         */
        private String character;

        /**
         * 是否为中文字符
         */
        private boolean chinese;

        /**
         * 带声调的拼音
         */
        private String pinyinWithTone;

        /**
         * 不带声调的拼音
         */
        private String pinyinWithoutTone;

        /**
         * 声母（首字母）
         */
        private String initial;

        /**
         * 多音字其他读音
         */
        private List<String> alternativeReadings;
    }
}
