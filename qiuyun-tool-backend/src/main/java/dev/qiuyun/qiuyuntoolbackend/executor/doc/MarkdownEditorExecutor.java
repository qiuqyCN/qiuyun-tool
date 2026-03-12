package dev.qiuyun.qiuyuntoolbackend.executor.doc;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Markdown 编辑器执行器
 * 支持：实时预览、HTML 导出、字数统计、目录生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarkdownEditorExecutor extends AbstractToolExecutor<MarkdownEditorExecutor.EditorRequest, MarkdownEditorExecutor.EditorResponse> {

    private static final Set<String> VALID_ACTIONS = Set.of("preview", "export", "stats", "toc");

    @Override
    public String getToolCode() {
        return "markdown-editor";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(EditorRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getMarkdown(), "Markdown 内容");
        validateNotEmpty(request.getAction(), "操作类型");
        validateEnum(request.getAction(), "操作类型", VALID_ACTIONS);
    }

    @Override
    protected EditorResponse doExecute(EditorRequest request, ToolContext context) throws Exception {
        String markdown = request.getMarkdown();
        String action = request.getAction();
        EditorOptions options = request.getOptions() != null ? request.getOptions() : new EditorOptions();

        EditorResponse response = new EditorResponse();
        response.setSuccess(true);
        response.setAction(action);

        switch (action) {
            case "preview":
                response.setHtml(convertToHtml(markdown, options));
                break;
            case "export":
                response.setHtml(convertToHtml(markdown, options));
                response.setCss(generateExportCss(options));
                break;
            case "stats":
                response.setStats(calculateStats(markdown));
                break;
            case "toc":
                response.setToc(generateToc(markdown));
                break;
            default:
                throw new BusinessException("不支持的操作类型: " + action);
        }

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "Markdown 编辑器操作失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "Markdown编辑器",
                "description", "在线 Markdown 编辑器，支持实时预览、导出、字数统计等功能",
                "actions", VALID_ACTIONS,
                "actionLabels", Map.of(
                        "preview", "实时预览",
                        "export", "导出HTML",
                        "stats", "字数统计",
                        "toc", "生成目录"
                ),
                "features", Map.of(
                        "syntaxHighlight", "代码高亮",
                        "tables", "表格支持",
                        "taskLists", "任务列表",
                        "emoji", "表情符号",
                        "footnotes", "脚注",
                        "toc", "自动生成目录",
                        "superscript", "上标/下标",
                        "typographic", "排版美化"
                ),
                "themes", List.of("default", "github", "dark")
        );
    }

    /**
     * Markdown 转 HTML
     */
    private String convertToHtml(String markdown, EditorOptions options) {
        MutableDataSet parserOptions = new MutableDataSet();
        List<com.vladsch.flexmark.util.misc.Extension> extensions = new ArrayList<>();

        // 核心插件（始终启用）
        extensions.add(TablesExtension.create());
        extensions.add(TaskListExtension.create());
        extensions.add(AnchorLinkExtension.create());
        extensions.add(AutolinkExtension.create());
        extensions.add(EmojiExtension.create());
        extensions.add(StrikethroughExtension.create());
        extensions.add(InsExtension.create());

        // 可选插件
        if (Boolean.TRUE.equals(options.getEnableFootnotes())) {
            extensions.add(FootnoteExtension.create());
        }
        if (Boolean.TRUE.equals(options.getEnableToc())) {
            extensions.add(TocExtension.create());
        }
        if (Boolean.TRUE.equals(options.getEnableSuperscript())) {
            extensions.add(SuperscriptExtension.create());
        }
        if (Boolean.TRUE.equals(options.getEnableTypographic())) {
            extensions.add(TypographicExtension.create());
        }
        if (Boolean.TRUE.equals(options.getEnableYamlFrontMatter())) {
            extensions.add(YamlFrontMatterExtension.create());
        }

        parserOptions.set(Parser.EXTENSIONS, extensions);

        // 代码高亮配置
        if (Boolean.TRUE.equals(options.getEnableSyntaxHighlight())) {
            parserOptions.set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "language-");
        }

        Parser parser = Parser.builder(parserOptions).build();
        HtmlRenderer renderer = HtmlRenderer.builder(parserOptions).build();

        com.vladsch.flexmark.util.ast.Node document = parser.parse(markdown);
        String html = renderer.render(document);

        // 根据主题包装 HTML
        return wrapHtmlWithTheme(html, options.getTheme());
    }

    /**
     * 根据主题包装 HTML
     */
    private String wrapHtmlWithTheme(String body, String theme) {
        String themeCss = getThemeCss(theme);
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<style>\n" +
                themeCss +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"markdown-body\">\n" +
                body +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 获取主题 CSS
     */
    private String getThemeCss(String theme) {
        String baseCss = """
            .markdown-body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans', Helvetica, Arial, sans-serif;
                font-size: 16px;
                line-height: 1.6;
                word-wrap: break-word;
                padding: 20px;
                max-width: 900px;
                margin: 0 auto;
            }
            .markdown-body h1, .markdown-body h2, .markdown-body h3,
            .markdown-body h4, .markdown-body h5, .markdown-body h6 {
                margin-top: 24px;
                margin-bottom: 16px;
                font-weight: 600;
                line-height: 1.25;
            }
            .markdown-body h1 { font-size: 2em; border-bottom: 1px solid; padding-bottom: 0.3em; }
            .markdown-body h2 { font-size: 1.5em; border-bottom: 1px solid; padding-bottom: 0.3em; }
            .markdown-body h3 { font-size: 1.25em; }
            .markdown-body h4 { font-size: 1em; }
            .markdown-body h5 { font-size: 0.875em; }
            .markdown-body h6 { font-size: 0.85em; }
            .markdown-body p { margin-top: 0; margin-bottom: 16px; }
            .markdown-body a { text-decoration: none; }
            .markdown-body a:hover { text-decoration: underline; }
            .markdown-body strong { font-weight: 600; }
            .markdown-body code {
                padding: 0.2em 0.4em;
                margin: 0;
                font-size: 85%;
                border-radius: 6px;
                font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
            }
            .markdown-body pre {
                padding: 16px;
                overflow: auto;
                font-size: 85%;
                line-height: 1.45;
                border-radius: 6px;
                margin-bottom: 16px;
            }
            .markdown-body pre code {
                display: inline;
                padding: 0;
                margin: 0;
                overflow: visible;
                line-height: inherit;
                word-wrap: normal;
                background-color: transparent;
                border: 0;
            }
            .markdown-body blockquote {
                padding: 0 1em;
                margin: 0 0 16px;
                border-left: 0.25em solid;
            }
            .markdown-body ul, .markdown-body ol {
                padding-left: 2em;
                margin-top: 0;
                margin-bottom: 16px;
            }
            .markdown-body li + li { margin-top: 0.25em; }
            .markdown-body table {
                border-spacing: 0;
                border-collapse: collapse;
                margin-bottom: 16px;
                width: 100%;
                overflow: auto;
                display: block;
            }
            .markdown-body table th, .markdown-body table td {
                padding: 6px 13px;
                border: 1px solid;
            }
            .markdown-body table tr { border-top: 1px solid; }
            .markdown-body img {
                max-width: 100%;
                box-sizing: content-box;
                background-color: #fff;
            }
            .markdown-body hr {
                height: 0.25em;
                padding: 0;
                margin: 24px 0;
                border: 0;
            }
            .markdown-body .task-list-item { list-style-type: none; }
            .markdown-body .task-list-item-checkbox { margin-right: 0.5em; }
            """;

        String themeSpecificCss = switch (theme != null ? theme : "default") {
            case "github" -> """
                .markdown-body { color: #24292f; background-color: #ffffff; }
                .markdown-body h1, .markdown-body h2 { border-bottom-color: #d0d7de; }
                .markdown-body a { color: #0969da; }
                .markdown-body code { background-color: rgba(175, 184, 193, 0.2); color: inherit; }
                .markdown-body pre { background-color: #f6f8fa; }
                .markdown-body blockquote { color: #57606a; border-left-color: #d0d7de; }
                .markdown-body table th, .markdown-body table td { border-color: #d0d7de; }
                .markdown-body table tr { background-color: #ffffff; border-top-color: #d0d7de; }
                .markdown-body table tr:nth-child(2n) { background-color: #f6f8fa; }
                .markdown-body hr { background-color: #d0d7de; }
                """;
            case "dark" -> """
                .markdown-body { color: #c9d1d9; background-color: #0d1117; }
                .markdown-body h1, .markdown-body h2 { border-bottom-color: #30363d; }
                .markdown-body a { color: #58a6ff; }
                .markdown-body code { background-color: rgba(110, 118, 129, 0.4); color: inherit; }
                .markdown-body pre { background-color: #161b22; }
                .markdown-body blockquote { color: #8b949e; border-left-color: #30363d; }
                .markdown-body table th, .markdown-body table td { border-color: #30363d; }
                .markdown-body table tr { background-color: #0d1117; border-top-color: #30363d; }
                .markdown-body table tr:nth-child(2n) { background-color: #161b22; }
                .markdown-body hr { background-color: #30363d; }
                """;
            default -> """
                .markdown-body { color: #333; background-color: #fafafa; }
                .markdown-body h1, .markdown-body h2 { border-bottom-color: #eaecef; }
                .markdown-body a { color: #0366d6; }
                .markdown-body code { background-color: #f3f4f6; color: #e83e8c; }
                .markdown-body pre { background-color: #f6f8fa; }
                .markdown-body blockquote { color: #6a737d; border-left-color: #dfe2e5; }
                .markdown-body table th, .markdown-body table td { border-color: #dfe2e5; }
                .markdown-body table tr { background-color: #fff; border-top-color: #c6cbd1; }
                .markdown-body table tr:nth-child(2n) { background-color: #f6f8fa; }
                .markdown-body table th { background-color: #f6f8fa; font-weight: 600; }
                .markdown-body hr { background-color: #e1e4e8; }
                """;
        };

        return baseCss + themeSpecificCss;
    }

    /**
     * 生成导出用 CSS
     */
    private String generateExportCss(EditorOptions options) {
        return getThemeCss(options.getTheme());
    }

    /**
     * 计算文档统计信息
     */
    private DocumentStats calculateStats(String markdown) {
        DocumentStats stats = new DocumentStats();
        
        // 字符数（包含空格和换行）
        stats.setTotalChars(markdown.length());
        
        // 字符数（不包含空格）
        stats.setCharsNoSpaces(markdown.replaceAll("\\s", "").length());
        
        // 行数
        stats.setLines(markdown.isEmpty() ? 0 : markdown.split("\\r?\\n").length);
        
        // 单词数（中文按字计算，英文按单词计算）
        String textOnly = markdown
                .replaceAll("```[\\s\\S]*?```", "") // 移除代码块
                .replaceAll("`[^`]*`", "") // 移除行内代码
                .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1") // 保留链接文本
                .replaceAll("!\\[([^\\]]*)\\]\\([^)]*\\)", "") // 移除图片
                .replaceAll("[#*_~>`\\-+|]", "") // 移除 Markdown 标记
                .trim();
        
        // 中文字符数
        int chineseChars = textOnly.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        
        // 英文单词数（按空白分割）
        String englishOnly = textOnly.replaceAll("[\\u4e00-\\u9fa5]", " ");
        int englishWords = englishOnly.trim().isEmpty() ? 0 : englishOnly.trim().split("\\s+").length;
        
        stats.setWords(chineseChars + englishWords);
        
        // 代码块行数
        int codeLines = 0;
        java.util.regex.Pattern codeBlockPattern = java.util.regex.Pattern.compile("```[\\s\\S]*?```");
        java.util.regex.Matcher matcher = codeBlockPattern.matcher(markdown);
        while (matcher.find()) {
            String codeBlock = matcher.group();
            codeLines += codeBlock.split("\\r?\\n").length - 2; // 减去 ``` 标记行
        }
        stats.setCodeLines(Math.max(0, codeLines));
        
        // 标题数
        int headings = 0;
        java.util.regex.Pattern headingPattern = java.util.regex.Pattern.compile("^#{1,6}\\s", java.util.regex.Pattern.MULTILINE);
        matcher = headingPattern.matcher(markdown);
        while (matcher.find()) {
            headings++;
        }
        stats.setHeadings(headings);
        
        // 图片数
        int images = 0;
        java.util.regex.Pattern imagePattern = java.util.regex.Pattern.compile("!\\[");
        matcher = imagePattern.matcher(markdown);
        while (matcher.find()) {
            images++;
        }
        stats.setImages(images);
        
        // 链接数
        int links = 0;
        java.util.regex.Pattern linkPattern = java.util.regex.Pattern.compile("(?<!!)\\[");
        matcher = linkPattern.matcher(markdown);
        while (matcher.find()) {
            links++;
        }
        stats.setLinks(links);
        
        // 表格数
        int tables = 0;
        java.util.regex.Pattern tablePattern = java.util.regex.Pattern.compile("\\|.*\\|.*\\|");
        matcher = tablePattern.matcher(markdown);
        while (matcher.find()) {
            tables++;
        }
        stats.setTables(tables / 2); // 每两个 | 行算一个表格
        
        return stats;
    }

    /**
     * 生成目录
     */
    private List<TocItem> generateToc(String markdown) {
        List<TocItem> toc = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(#{1,6})\\s+(.+)$", java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher matcher = pattern.matcher(markdown);
        
        while (matcher.find()) {
            int level = matcher.group(1).length();
            String title = matcher.group(2).trim();
            String anchor = title.toLowerCase()
                    .replaceAll("[^\\w\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
            
            toc.add(new TocItem(title, anchor, level));
        }
        
        return toc;
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class EditorRequest {
        private String markdown;
        private String action;
        private EditorOptions options;
    }

    @Data
    public static class EditorOptions {
        private String theme = "default";
        private Boolean enableSyntaxHighlight = true;
        private Boolean enableFootnotes = false;
        private Boolean enableToc = false;
        private Boolean enableSuperscript = false;
        private Boolean enableTypographic = false;
        private Boolean enableYamlFrontMatter = false;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EditorResponse extends BaseToolResponse {
        private String action;
        private String html;
        private String css;
        private DocumentStats stats;
        private List<TocItem> toc;
    }

    @Data
    public static class DocumentStats {
        private int totalChars;
        private int charsNoSpaces;
        private int words;
        private int lines;
        private int codeLines;
        private int headings;
        private int images;
        private int links;
        private int tables;
    }

    @Data
    public static class TocItem {
        private String title;
        private String anchor;
        private int level;

        public TocItem(String title, String anchor, int level) {
            this.title = title;
            this.anchor = anchor;
            this.level = level;
        }
    }
}
