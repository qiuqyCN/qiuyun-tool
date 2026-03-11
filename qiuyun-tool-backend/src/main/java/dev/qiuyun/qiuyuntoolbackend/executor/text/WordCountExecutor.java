package dev.qiuyun.qiuyuntoolbackend.executor.text;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字数统计工具执行器
 * 支持：字数、字符数、行数、词数、段落数、阅读时间等统计
 */
@Slf4j
@Component
public class WordCountExecutor implements ToolExecutor<WordCountExecutor.WordCountRequest, WordCountExecutor.WordCountResponse> {

    @Override
    public String getToolCode() {
        return "word-count";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(WordCountRequest request) throws BusinessException {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getText() == null) {
            request.setText("");
        }
    }

    @Override
    public WordCountResponse execute(WordCountRequest request, ToolContext context) throws BusinessException {
        try {
            String text = request.getText();
            boolean includeSpaces = request.isIncludeSpaces();

            WordCountResponse response = new WordCountResponse();
            response.setSuccess(true);

            // 基础统计
            BasicStats basicStats = calculateBasicStats(text, includeSpaces);
            response.setBasicStats(basicStats);

            // 字符统计
            CharStats charStats = calculateCharStats(text);
            response.setCharStats(charStats);

            // 中文统计
            ChineseStats chineseStats = calculateChineseStats(text);
            response.setChineseStats(chineseStats);

            // 英文统计
            EnglishStats englishStats = calculateEnglishStats(text);
            response.setEnglishStats(englishStats);

            // 数字统计
            NumberStats numberStats = calculateNumberStats(text);
            response.setNumberStats(numberStats);

            // 阅读时间估算
            ReadingTime readingTime = calculateReadingTime(text);
            response.setReadingTime(readingTime);

            // 词频统计（前10个）
            if (request.isShowWordFrequency()) {
                List<WordFrequency> wordFrequencies = calculateWordFrequency(text);
                response.setWordFrequencies(wordFrequencies);
            }

            return response;

        } catch (Exception e) {
            log.error("字数统计错误: {}", e.getMessage());
            throw new BusinessException("统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "字数统计",
                "description", "统计文本字数、字符数、行数、阅读时间等",
                "features", new String[]{"wordFrequency", "readingTime", "chineseEnglish"}
        );
    }

    /**
     * 基础统计
     */
    private BasicStats calculateBasicStats(String text, boolean includeSpaces) {
        BasicStats stats = new BasicStats();

        // 总字符数（含空格）
        stats.setTotalChars(text.length());

        // 总字符数（不含空格）
        stats.setCharsWithoutSpaces(text.replaceAll("\\s", "").length());

        // 行数
        stats.setLines(text.isEmpty() ? 0 : text.split("\\r?\\n").length);

        // 段落数（按空行分割）
        String[] paragraphs = text.split("\\n\\s*\\n");
        stats.setParagraphs(text.trim().isEmpty() ? 0 : paragraphs.length);

        // 字节数
        stats.setBytes(text.getBytes().length);

        return stats;
    }

    /**
     * 字符统计
     */
    private CharStats calculateCharStats(String text) {
        CharStats stats = new CharStats();

        // 空格数
        stats.setSpaces(countMatches(text, " "));

        // 换行数
        stats.setNewlines(countMatches(text, "\\n"));

        // 标点符号数
        String punctuation = "，。、；：？！\"'\"'（）【】《》…—·\\.,;:!?\"'()[]{}";
        long punctuationCount = text.chars().filter(c -> punctuation.indexOf(c) >= 0).count();
        stats.setPunctuation((int) punctuationCount);

        // 特殊字符数
        long specialChars = text.chars().filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c) && punctuation.indexOf(c) < 0).count();
        stats.setSpecialChars((int) specialChars);

        return stats;
    }

    /**
     * 中文统计
     */
    private ChineseStats calculateChineseStats(String text) {
        ChineseStats stats = new ChineseStats();

        // 中文字符数（CJK统一表意文字）
        Pattern chinesePattern = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher matcher = chinesePattern.matcher(text);
        int chineseChars = 0;
        while (matcher.find()) {
            chineseChars++;
        }
        stats.setChineseChars(chineseChars);

        // 中文标点
        String chinesePunctuation = "，。、；：？！\"'\"'（）【】《》…—·";
        long chinesePunctCount = text.chars().filter(c -> chinesePunctuation.indexOf(c) >= 0).count();
        stats.setChinesePunctuation((int) chinesePunctCount);

        return stats;
    }

    /**
     * 英文统计
     */
    private EnglishStats calculateEnglishStats(String text) {
        EnglishStats stats = new EnglishStats();

        // 英文单词数
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = wordPattern.matcher(text);
        int wordCount = 0;
        int letterCount = 0;
        while (matcher.find()) {
            wordCount++;
            letterCount += matcher.group().length();
        }
        stats.setEnglishWords(wordCount);
        stats.setEnglishLetters(letterCount);

        return stats;
    }

    /**
     * 数字统计
     */
    private NumberStats calculateNumberStats(String text) {
        NumberStats stats = new NumberStats();

        // 数字个数
        Pattern numberPattern = Pattern.compile("\\d");
        Matcher matcher = numberPattern.matcher(text);
        int digitCount = 0;
        while (matcher.find()) {
            digitCount++;
        }
        stats.setDigits(digitCount);

        // 数字组数（连续数字）
        Pattern numberGroupPattern = Pattern.compile("\\d+");
        Matcher groupMatcher = numberGroupPattern.matcher(text);
        int groupCount = 0;
        while (groupMatcher.find()) {
            groupCount++;
        }
        stats.setNumberGroups(groupCount);

        return stats;
    }

    /**
     * 阅读时间估算
     */
    private ReadingTime calculateReadingTime(String text) {
        ReadingTime time = new ReadingTime();

        // 中文字符数
        int chineseChars = calculateChineseStats(text).getChineseChars();

        // 英文单词数
        int englishWords = calculateEnglishStats(text).getEnglishWords();

        // 阅读速度：中文约 400 字/分钟，英文约 200 词/分钟
        double minutes = (chineseChars / 400.0) + (englishWords / 200.0);

        if (minutes < 1) {
            time.setSeconds((int) (minutes * 60));
            time.setFormattedTime(time.getSeconds() + " 秒");
        } else if (minutes < 60) {
            time.setMinutes((int) minutes);
            time.setFormattedTime(time.getMinutes() + " 分钟");
        } else {
            int hours = (int) (minutes / 60);
            int mins = (int) (minutes % 60);
            time.setHours(hours);
            time.setMinutes(mins);
            time.setFormattedTime(hours + " 小时 " + mins + " 分钟");
        }

        return time;
    }

    /**
     * 词频统计
     */
    private List<WordFrequency> calculateWordFrequency(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        // 中文词语（简单按字统计）
        Pattern chinesePattern = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher chineseMatcher = chinesePattern.matcher(text);
        while (chineseMatcher.find()) {
            String word = chineseMatcher.group();
            frequencyMap.merge(word, 1, Integer::sum);
        }

        // 英文单词
        Pattern englishPattern = Pattern.compile("[a-zA-Z]+");
        Matcher englishMatcher = englishPattern.matcher(text);
        while (englishMatcher.find()) {
            String word = englishMatcher.group().toLowerCase();
            frequencyMap.merge(word, 1, Integer::sum);
        }

        // 排序并取前10
        return frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    WordFrequency wf = new WordFrequency();
                    wf.setWord(entry.getKey());
                    wf.setCount(entry.getValue());
                    return wf;
                })
                .collect(Collectors.toList());
    }

    /**
     * 统计匹配次数
     */
    private int countMatches(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    /**
     * 请求参数
     */
    @Data
    public static class WordCountRequest {
        /**
         * 输入文本
         */
        private String text;
        /**
         * 是否包含空格统计
         */
        private boolean includeSpaces = true;
        /**
         * 是否显示词频统计
         */
        private boolean showWordFrequency = false;
    }

    /**
     * 响应结果
     */
    @Data
    public static class WordCountResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 基础统计
         */
        private BasicStats basicStats;
        /**
         * 字符统计
         */
        private CharStats charStats;
        /**
         * 中文统计
         */
        private ChineseStats chineseStats;
        /**
         * 英文统计
         */
        private EnglishStats englishStats;
        /**
         * 数字统计
         */
        private NumberStats numberStats;
        /**
         * 阅读时间
         */
        private ReadingTime readingTime;
        /**
         * 词频统计
         */
        private List<WordFrequency> wordFrequencies;
        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 基础统计
     */
    @Data
    public static class BasicStats {
        /**
         * 总字符数（含空格）
         */
        private int totalChars;
        /**
         * 字符数（不含空格）
         */
        private int charsWithoutSpaces;
        /**
         * 行数
         */
        private int lines;
        /**
         * 段落数
         */
        private int paragraphs;
        /**
         * 字节数
         */
        private int bytes;
    }

    /**
     * 字符统计
     */
    @Data
    public static class CharStats {
        /**
         * 空格数
         */
        private int spaces;
        /**
         * 换行数
         */
        private int newlines;
        /**
         * 标点符号数
         */
        private int punctuation;
        /**
         * 特殊字符数
         */
        private int specialChars;
    }

    /**
     * 中文统计
     */
    @Data
    public static class ChineseStats {
        /**
         * 中文字符数
         */
        private int chineseChars;
        /**
         * 中文标点符号数
         */
        private int chinesePunctuation;
    }

    /**
     * 英文统计
     */
    @Data
    public static class EnglishStats {
        /**
         * 英文单词数
         */
        private int englishWords;
        /**
         * 英文字母数
         */
        private int englishLetters;
    }

    /**
     * 数字统计
     */
    @Data
    public static class NumberStats {
        /**
         * 数字个数
         */
        private int digits;
        /**
         * 数字组数
         */
        private int numberGroups;
    }

    /**
     * 阅读时间
     */
    @Data
    public static class ReadingTime {
        /**
         * 秒数
         */
        private int seconds;
        /**
         * 分钟数
         */
        private int minutes;
        /**
         * 小时数
         */
        private int hours;
        /**
         * 格式化时间
         */
        private String formattedTime;
    }

    /**
     * 词频
     */
    @Data
    public static class WordFrequency {
        /**
         * 词语
         */
        private String word;
        /**
         * 出现次数
         */
        private int count;
    }
}
