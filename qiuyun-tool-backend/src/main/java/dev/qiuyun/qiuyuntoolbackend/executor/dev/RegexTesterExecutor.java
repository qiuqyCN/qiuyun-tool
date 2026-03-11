package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 正则表达式测试工具执行器
 * 支持：正则匹配、分组提取、常用预设
 */
@Slf4j
@Component
public class RegexTesterExecutor implements ToolExecutor<RegexTesterExecutor.RegexTesterRequest, RegexTesterExecutor.RegexTesterResponse> {

    @Override
    public String getToolCode() {
        return "regex-tester";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(RegexTesterRequest request) throws BusinessException {
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getPattern() == null || request.getPattern().trim().isEmpty()) {
            throw new BusinessException("正则表达式不能为空");
        }
        if (request.getText() == null) {
            throw new BusinessException("测试文本不能为空");
        }
    }

    @Override
    public RegexTesterResponse execute(RegexTesterRequest request, ToolContext context) throws BusinessException {
        String pattern = request.getPattern();
        String text = request.getText();
        List<String> flags = request.getFlags() != null ? request.getFlags() : new ArrayList<>();

        try {
            // 构建正则表达式
            int regexFlags = buildRegexFlags(flags);
            Pattern compiledPattern = Pattern.compile(pattern, regexFlags);
            Matcher matcher = compiledPattern.matcher(text);

            // 执行匹配
            List<MatchResult> matches = new ArrayList<>();
            boolean isMatch = false;

            if (request.isFindAll()) {
                // 查找所有匹配
                while (matcher.find()) {
                    isMatch = true;
                    matches.add(buildMatchResult(matcher, text));
                }
            } else {
                // 只查找第一个匹配
                if (matcher.find()) {
                    isMatch = true;
                    matches.add(buildMatchResult(matcher, text));
                }
            }

            // 构建响应
            RegexTesterResponse response = new RegexTesterResponse();
            response.setSuccess(true);
            response.setIsMatch(isMatch);
            response.setMatchCount(matches.size());
            response.setMatches(matches);
            response.setHighlightedText(buildHighlightedText(text, matches));

            return response;

        } catch (PatternSyntaxException e) {
            log.error("正则表达式语法错误: {}", e.getMessage());
            throw new BusinessException("正则表达式语法错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("正则测试错误: {}", e.getMessage());
            throw new BusinessException("测试失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "正则测试",
                "description", "在线正则表达式测试工具",
                "flags", new String[]{"i", "m", "s", "x"},
                "flagLabels", Map.of(
                        "i", "忽略大小写 (CASE_INSENSITIVE)",
                        "m", "多行模式 (MULTILINE)",
                        "s", "单行模式 (DOTALL)",
                        "x", "忽略空白 (COMMENTS)"
                ),
                "presets", getRegexPresets()
        );
    }

    /**
     * 获取常用正则预设
     */
    private Map<String, String> getRegexPresets() {
        Map<String, String> presets = new HashMap<>();
        presets.put("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        presets.put("phone", "^1[3-9]\\d{9}$");
        presets.put("url", "^https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+$");
        presets.put("ip", "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        presets.put("idCard", "^[1-9]\\d{5}(?:18|19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
        presets.put("chinese", "^[\\u4e00-\\u9fa5]+$");
        presets.put("number", "^-?\\d+\\.?\\d*$");
        presets.put("date", "^\\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12]\\d|3[01])$");
        presets.put("htmlTag", "<[^>]+>");
        presets.put("whitespace", "\\s+");
        return presets;
    }

    /**
     * 构建正则标志位
     */
    private int buildRegexFlags(List<String> flags) {
        int regexFlags = 0;
        for (String flag : flags) {
            switch (flag.toLowerCase()) {
                case "i":
                    regexFlags |= Pattern.CASE_INSENSITIVE;
                    break;
                case "m":
                    regexFlags |= Pattern.MULTILINE;
                    break;
                case "s":
                    regexFlags |= Pattern.DOTALL;
                    break;
                case "x":
                    regexFlags |= Pattern.COMMENTS;
                    break;
            }
        }
        return regexFlags;
    }

    /**
     * 构建匹配结果
     */
    private MatchResult buildMatchResult(Matcher matcher, String text) {
        MatchResult result = new MatchResult();
        result.setFullMatch(matcher.group());
        result.setStart(matcher.start());
        result.setEnd(matcher.end());

        // 提取分组
        List<MatchGroup> groups = new ArrayList<>();
        for (int i = 0; i <= matcher.groupCount(); i++) {
            MatchGroup group = new MatchGroup();
            group.setIndex(i);
            group.setValue(matcher.group(i));
            group.setStart(matcher.start(i));
            group.setEnd(matcher.end(i));
            groups.add(group);
        }
        result.setGroups(groups);

        return result;
    }

    /**
     * 构建高亮文本
     */
    private String buildHighlightedText(String text, List<MatchResult> matches) {
        if (matches.isEmpty()) {
            return escapeHtml(text);
        }

        StringBuilder highlighted = new StringBuilder();
        int lastEnd = 0;

        for (MatchResult match : matches) {
            // 添加匹配前的文本
            if (match.getStart() > lastEnd) {
                highlighted.append(escapeHtml(text.substring(lastEnd, match.getStart())));
            }

            // 添加高亮的匹配文本
            highlighted.append("<mark class=\"bg-yellow-200 text-yellow-900 px-1 rounded\">");
            highlighted.append(escapeHtml(match.getFullMatch()));
            highlighted.append("</mark>");

            lastEnd = match.getEnd();
        }

        // 添加最后剩余的文本
        if (lastEnd < text.length()) {
            highlighted.append(escapeHtml(text.substring(lastEnd)));
        }

        return highlighted.toString();
    }

    /**
     * HTML 转义
     */
    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * 请求参数
     */
    @Data
    public static class RegexTesterRequest {
        /**
         * 正则表达式
         */
        private String pattern;
        /**
         * 测试文本
         */
        private String text;
        /**
         * 标志位: i(忽略大小写), m(多行), s(单行), x(忽略空白)
         */
        private List<String> flags;
        /**
         * 是否查找所有匹配
         */
        private boolean findAll = true;
    }

    /**
     * 响应结果
     */
    @Data
    public static class RegexTesterResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 是否匹配成功
         */
        private Boolean isMatch;
        /**
         * 匹配数量
         */
        private int matchCount;
        /**
         * 匹配结果列表
         */
        private List<MatchResult> matches;
        /**
         * 高亮显示的文本
         */
        private String highlightedText;
        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 匹配结果
     */
    @Data
    public static class MatchResult {
        /**
         * 完整匹配内容
         */
        private String fullMatch;
        /**
         * 匹配开始位置
         */
        private int start;
        /**
         * 匹配结束位置
         */
        private int end;
        /**
         * 分组信息
         */
        private List<MatchGroup> groups;
    }

    /**
     * 匹配分组
     */
    @Data
    public static class MatchGroup {
        /**
         * 分组索引 (0表示完整匹配)
         */
        private int index;
        /**
         * 分组值
         */
        private String value;
        /**
         * 开始位置
         */
        private int start;
        /**
         * 结束位置
         */
        private int end;
    }
}
