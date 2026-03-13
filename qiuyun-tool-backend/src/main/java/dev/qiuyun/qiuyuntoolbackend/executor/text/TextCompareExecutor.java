package dev.qiuyun.qiuyuntoolbackend.executor.text;

import dev.qiuyun.qiuyuntoolbackend.enums.CompareMode;
import dev.qiuyun.qiuyuntoolbackend.enums.DiffType;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文本对比工具执行器
 * 支持：行对比、字符对比、差异高亮
 */
@Slf4j
@Component
public class TextCompareExecutor extends AbstractToolExecutor<TextCompareExecutor.TextCompareRequest, TextCompareExecutor.TextCompareResponse> {


    @Override
    public String getToolCode() {
        return "text-compare";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(TextCompareRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        if (request.getOldText() == null) {
            request.setOldText("");
        }
        if (request.getNewText() == null) {
            request.setNewText("");
        }
    }

    @Override
    protected TextCompareResponse doExecute(TextCompareRequest request, ToolContext context) throws Exception {
        String oldText = request.getOldText();
        String newText = request.getNewText();
        String mode = request.getMode() != null ? request.getMode() : CompareMode.LINE.getCode();

        TextCompareResponse response = new TextCompareResponse();
        response.setSuccess(true);
        response.setMode(mode);

        // 计算统计信息
        TextStats oldStats = calculateStats(oldText);
        TextStats newStats = calculateStats(newText);
        response.setOldStats(oldStats);
        response.setNewStats(newStats);

        // 执行对比
        List<DiffLine> diffLines;
        if (CompareMode.CHAR.getCode().equals(mode)) {
            diffLines = compareByChar(oldText, newText);
        } else {
            diffLines = compareByLine(oldText, newText);
        }

        response.setDiffLines(diffLines);

        // 计算差异统计
        int added = 0, removed = 0, modified = 0;
        for (DiffLine line : diffLines) {
            if (DiffType.ADDED.getCode().equals(line.getType())) {
                added++;
            } else if (DiffType.REMOVED.getCode().equals(line.getType())) {
                removed++;
            } else if (DiffType.MODIFIED.getCode().equals(line.getType())) {
                modified++;
            }
        }

        DiffStats diffStats = new DiffStats();
        diffStats.setAdded(added);
        diffStats.setRemoved(removed);
        diffStats.setModified(modified);
        diffStats.setUnchanged(diffLines.size() - added - removed - modified);
        response.setDiffStats(diffStats);

        return response;
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "文本对比",
                "description", "文本差异对比工具，支持行对比和字符对比",
                "modes", new String[]{CompareMode.LINE.getCode(), CompareMode.CHAR.getCode()},
                "modeLabels", Map.of(
                        CompareMode.LINE.getCode(), CompareMode.LINE.getLabel(),
                        CompareMode.CHAR.getCode(), CompareMode.CHAR.getLabel()
                )
        );
    }

    /**
     * 按行对比
     */
    private List<DiffLine> compareByLine(String oldText, String newText) {
        String[] oldLines = oldText.split("\r?\n", -1);
        String[] newLines = newText.split("\r?\n", -1);

        List<DiffLine> result = new ArrayList<>();
        int oldIndex = 0, newIndex = 0;

        while (oldIndex < oldLines.length || newIndex < newLines.length) {
            String oldLine = oldIndex < oldLines.length ? oldLines[oldIndex] : null;
            String newLine = newIndex < newLines.length ? newLines[newIndex] : null;

            if (oldLine == null) {
                // 新增行
                DiffLine diff = new DiffLine();
                diff.setType(DiffType.ADDED.getCode());
                diff.setNewLineNum(newIndex + 1);
                diff.setNewContent(newLine);
                result.add(diff);
                newIndex++;
            } else if (newLine == null) {
                // 删除行
                DiffLine diff = new DiffLine();
                diff.setType(DiffType.REMOVED.getCode());
                diff.setOldLineNum(oldIndex + 1);
                diff.setOldContent(oldLine);
                result.add(diff);
                oldIndex++;
            } else if (oldLine.equals(newLine)) {
                // 未变更
                DiffLine diff = new DiffLine();
                diff.setType(DiffType.UNCHANGED.getCode());
                diff.setOldLineNum(oldIndex + 1);
                diff.setNewLineNum(newIndex + 1);
                diff.setOldContent(oldLine);
                diff.setNewContent(newLine);
                result.add(diff);
                oldIndex++;
                newIndex++;
            } else {
                // 查找下一个匹配行
                int oldMatch = findMatch(oldLines, oldIndex, newLines, newIndex);
                int newMatch = findMatch(newLines, newIndex, oldLines, oldIndex);

                if (oldMatch == -1 && newMatch == -1) {
                    // 修改行
                    DiffLine diff = new DiffLine();
                    diff.setType(DiffType.MODIFIED.getCode());
                    diff.setOldLineNum(oldIndex + 1);
                    diff.setNewLineNum(newIndex + 1);
                    diff.setOldContent(oldLine);
                    diff.setNewContent(newLine);
                    diff.setCharDiffs(compareChars(oldLine, newLine));
                    result.add(diff);
                    oldIndex++;
                    newIndex++;
                } else if (oldMatch == -1 || (newMatch != -1 && newMatch - newIndex <= oldMatch - oldIndex)) {
                    // 删除行
                    DiffLine diff = new DiffLine();
                    diff.setType(DiffType.REMOVED.getCode());
                    diff.setOldLineNum(oldIndex + 1);
                    diff.setOldContent(oldLine);
                    result.add(diff);
                    oldIndex++;
                } else {
                    // 新增行
                    DiffLine diff = new DiffLine();
                    diff.setType(DiffType.ADDED.getCode());
                    diff.setNewLineNum(newIndex + 1);
                    diff.setNewContent(newLine);
                    result.add(diff);
                    newIndex++;
                }
            }
        }

        return result;
    }

    /**
     * 查找匹配行
     */
    private int findMatch(String[] source, int sourceStart, String[] target, int targetStart) {
        if (sourceStart >= source.length) return -1;
        String toFind = source[sourceStart];

        for (int i = targetStart; i < target.length && i < targetStart + 10; i++) {
            if (toFind.equals(target[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 按字符对比
     */
    private List<DiffLine> compareByChar(String oldText, String newText) {
        List<DiffLine> result = new ArrayList<>();
        List<CharDiff> charDiffs = compareChars(oldText, newText);

        DiffLine diff = new DiffLine();
        diff.setType(DiffType.MODIFIED.getCode());
        diff.setOldLineNum(1);
        diff.setNewLineNum(1);
        diff.setOldContent(oldText);
        diff.setNewContent(newText);
        diff.setCharDiffs(charDiffs);
        result.add(diff);

        return result;
    }

    /**
     * 字符级对比
     */
    private List<CharDiff> compareChars(String oldStr, String newStr) {
        List<CharDiff> result = new ArrayList<>();
        int oldLen = oldStr.length();
        int newLen = newStr.length();

        // 使用简单的LCS算法
        int[][] dp = new int[oldLen + 1][newLen + 1];

        for (int i = 1; i <= oldLen; i++) {
            for (int j = 1; j <= newLen; j++) {
                if (oldStr.charAt(i - 1) == newStr.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // 回溯生成差异
        int i = oldLen, j = newLen;
        List<CharDiff> temp = new ArrayList<>();

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldStr.charAt(i - 1) == newStr.charAt(j - 1)) {
                CharDiff cd = new CharDiff();
                cd.setType(DiffType.UNCHANGED.getCode());
                cd.setOldChar(String.valueOf(oldStr.charAt(i - 1)));
                cd.setNewChar(String.valueOf(newStr.charAt(j - 1)));
                cd.setOldIndex(i - 1);
                cd.setNewIndex(j - 1);
                temp.add(cd);
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                CharDiff cd = new CharDiff();
                cd.setType(DiffType.ADDED.getCode());
                cd.setNewChar(String.valueOf(newStr.charAt(j - 1)));
                cd.setNewIndex(j - 1);
                temp.add(cd);
                j--;
            } else {
                CharDiff cd = new CharDiff();
                cd.setType(DiffType.REMOVED.getCode());
                cd.setOldChar(String.valueOf(oldStr.charAt(i - 1)));
                cd.setOldIndex(i - 1);
                temp.add(cd);
                i--;
            }
        }

        // 反转结果
        for (int k = temp.size() - 1; k >= 0; k--) {
            result.add(temp.get(k));
        }

        return result;
    }

    /**
     * 计算文本统计
     */
    private TextStats calculateStats(String text) {
        TextStats stats = new TextStats();
        stats.setLength(text.length());
        stats.setLines(text.isEmpty() ? 0 : text.split("\r?\n").length);
        stats.setWords(text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length);
        stats.setChars(text.replaceAll("\\s", "").length());
        return stats;
    }

    /**
     * 请求参数
     */
    @Data
    public static class TextCompareRequest {
        /**
         * 旧文本
         */
        private String oldText;
        /**
         * 新文本
         */
        private String newText;
        /**
         * 对比模式：line(行对比)、char(字符对比)
         */
        private String mode = CompareMode.LINE.getCode();
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TextCompareResponse extends BaseToolResponse {
        /**
         * 对比模式
         */
        private String mode;
        /**
         * 旧文本统计
         */
        private TextStats oldStats;
        /**
         * 新文本统计
         */
        private TextStats newStats;
        /**
         * 差异统计
         */
        private DiffStats diffStats;
        /**
         * 差异行列表
         */
        private List<DiffLine> diffLines;
    }

    /**
     * 文本统计
     */
    @Data
    public static class TextStats {
        /**
         * 字符数
         */
        private int length;
        /**
         * 行数
         */
        private int lines;
        /**
         * 词数
         */
        private int words;
        /**
         * 非空白字符数
         */
        private int chars;
    }

    /**
     * 差异统计
     */
    @Data
    public static class DiffStats {
        /**
         * 新增行数
         */
        private int added;
        /**
         * 删除行数
         */
        private int removed;
        /**
         * 修改行数
         */
        private int modified;
        /**
         * 未变更行数
         */
        private int unchanged;
    }

    /**
     * 差异行
     */
    @Data
    public static class DiffLine {
        /**
         * 类型：added(新增)、removed(删除)、modified(修改)、unchanged(未变更)
         */
        private String type;
        /**
         * 旧行号
         */
        private Integer oldLineNum;
        /**
         * 新行号
         */
        private Integer newLineNum;
        /**
         * 旧内容
         */
        private String oldContent;
        /**
         * 新内容
         */
        private String newContent;
        /**
         * 字符级差异
         */
        private List<CharDiff> charDiffs;
    }

    /**
     * 字符差异
     */
    @Data
    public static class CharDiff {
        /**
         * 类型：added(新增)、removed(删除)、unchanged(未变更)
         */
        private String type;
        /**
         * 旧字符
         */
        private String oldChar;
        /**
         * 新字符
         */
        private String newChar;
        /**
         * 旧索引
         */
        private Integer oldIndex;
        /**
         * 新索引
         */
        private Integer newIndex;
    }
}
