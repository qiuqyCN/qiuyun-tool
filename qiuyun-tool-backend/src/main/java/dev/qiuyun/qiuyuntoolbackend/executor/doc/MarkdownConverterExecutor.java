package dev.qiuyun.qiuyuntoolbackend.executor.doc;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
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
import org.docx4j.Docx4J;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * Markdown 格式转换工具执行器
 * 支持：Markdown 转 HTML、PDF、Word
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarkdownConverterExecutor extends AbstractToolExecutor<MarkdownConverterExecutor.ConverterRequest, MarkdownConverterExecutor.ConverterResponse> {

    private static final Set<String> VALID_FORMATS = Set.of("html", "pdf", "word");

    @Override
    public String getToolCode() {
        return "markdown-converter";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ConverterRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getMarkdown(), "Markdown 内容");
        validateNotEmpty(request.getTargetFormat(), "目标格式");
        validateEnum(request.getTargetFormat(), "目标格式", VALID_FORMATS);
    }

    @Override
    protected ConverterResponse doExecute(ConverterRequest request, ToolContext context) throws Exception {
        String markdown = request.getMarkdown();
        String targetFormat = request.getTargetFormat();
        java.util.List<String> enabledPlugins = request.getEnabledPlugins();

        String result;
        String contentType;
        String fileExtension;

        switch (targetFormat) {
            case "html":
                result = convertToHtml(markdown, enabledPlugins);
                contentType = "text/html";
                fileExtension = "html";
                break;
            case "pdf":
                result = convertToPdf(markdown, enabledPlugins);
                contentType = "application/pdf";
                fileExtension = "pdf";
                break;
            case "word":
                result = convertToWord(markdown, enabledPlugins);
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                fileExtension = "docx";
                break;
            default:
                throw new BusinessException("不支持的目标格式: " + targetFormat);
        }

        ConverterResponse response = new ConverterResponse();
        response.setSuccess(true);
        response.setResult(result);
        response.setTargetFormat(targetFormat);
        response.setContentType(contentType);
        response.setFileExtension(fileExtension);

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "Markdown 转换失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "Markdown格式转换",
                "description", "Markdown转HTML、PDF、Word等格式",
                "targetFormats", VALID_FORMATS,
                "formatLabels", Map.of(
                        "html", "HTML",
                        "pdf", "PDF",
                        "word", "Word (DOCX)"
                ),
                "optionalPlugins", Map.of(
                        "footnotes", "脚注支持",
                        "toc", "自动生成目录",
                        "superscript", "上标/下标",
                        "typographic", "排版美化",
                        "yamlFrontMatter", "YAML前置元数据"
                )
        );
    }

    /**
     * Markdown 转 HTML
     */
    private String convertToHtml(String markdown, java.util.List<String> enabledPlugins) {
        MutableDataSet options = new MutableDataSet();
        java.util.List<com.vladsch.flexmark.util.misc.Extension> extensions = new java.util.ArrayList<>();

        // 核心插件（始终启用）
        extensions.add(TablesExtension.create());  // 表格
        extensions.add(TaskListExtension.create());  // 任务列表
        extensions.add(AnchorLinkExtension.create());  // 锚点链接
        extensions.add(AutolinkExtension.create());    // 自动链接
        extensions.add(EmojiExtension.create());  // emoji
        extensions.add(StrikethroughExtension.create());  // 删除线
        extensions.add(InsExtension.create()); // 插入标记

        // 可选插件
        if (enabledPlugins != null) {
            for (String plugin : enabledPlugins) {
                switch (plugin) {
                    case "footnotes":
                        extensions.add(com.vladsch.flexmark.ext.footnotes.FootnoteExtension.create());
                        break;
                    case "toc":
                        extensions.add(com.vladsch.flexmark.ext.toc.TocExtension.create());
                        break;
                    case "superscript":
                        extensions.add(com.vladsch.flexmark.ext.superscript.SuperscriptExtension.create());
                        break;
                    case "typographic":
                        extensions.add(com.vladsch.flexmark.ext.typographic.TypographicExtension.create());
                        break;
                    case "yamlFrontMatter":
                        extensions.add(com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension.create());
                        break;
                }
            }
        }

        options.set(Parser.EXTENSIONS, extensions);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        com.vladsch.flexmark.util.ast.Node document = parser.parse(markdown);
        String html = renderer.render(document);

        // 包装成完整的 HTML 文档
        return wrapHtmlDocument(html);
    }

    /**
     * 包装成完整的 HTML 文档（XHTML 格式，兼容 openhtmltopdf）
     */
    private String wrapHtmlDocument(String body) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\" />\n" +
                "<style>\n" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; color: #333; }\n" +
                "h1, h2, h3, h4, h5, h6 { margin-top: 24px; margin-bottom: 16px; font-weight: 600; line-height: 1.25; }\n" +
                "h1 { font-size: 2em; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }\n" +
                "h2 { font-size: 1.5em; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }\n" +
                "code { background-color: #f6f8fa; padding: 0.2em 0.4em; border-radius: 3px; font-family: 'SFMono-Regular', Consolas, monospace; }\n" +
                "pre { background-color: #f6f8fa; padding: 16px; overflow: auto; border-radius: 6px; }\n" +
                "pre code { background-color: transparent; padding: 0; }\n" +
                "blockquote { margin: 0; padding: 0 1em; color: #6a737d; border-left: 0.25em solid #dfe2e5; }\n" +
                "table { border-collapse: collapse; width: 100%; margin-bottom: 16px; }\n" +
                "table th, table td { padding: 6px 13px; border: 1px solid #dfe2e5; }\n" +
                "table th { background-color: #f6f8fa; font-weight: 600; }\n" +
                "ul, ol { padding-left: 2em; }\n" +
                "a { color: #0366d6; text-decoration: none; }\n" +
                "a:hover { text-decoration: underline; }\n" +
                "img { max-width: 100%; box-sizing: border-box; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                body +
                "</body>\n" +
                "</html>";
    }

    /**
     * Markdown 转 PDF
     */
    private String convertToPdf(String markdown, java.util.List<String> enabledPlugins) throws Exception {
        String html = convertToHtml(markdown, enabledPlugins);
        
        // 为 PDF 添加中文字体支持
        html = addPdfFontSupport(html);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        
        // 尝试加载系统自带的中文字体文件
        loadSystemFonts(builder);
        
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        builder.run();

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    
    /**
     * 为 PDF 添加中文字体支持
     */
    private String addPdfFontSupport(String html) {
        // 检测操作系统
        String osName = System.getProperty("os.name").toLowerCase();
        String fontFamily;
        
        if (osName.contains("win")) {
            // Windows 字体回退
            fontFamily = "'SimHei', 'SimSun', 'Microsoft YaHei', sans-serif";
        } else if (osName.contains("mac")) {
            // macOS 字体回退
            fontFamily = "'PingFang SC', 'STHeiti', 'Hiragino Sans GB', sans-serif";
        } else {
            // Linux 字体回退
            fontFamily = "'WenQuanYi Zen Hei', 'WenQuanYi Micro Hei', 'Noto Sans CJK', 'DejaVu Sans', sans-serif";
        }
        
        // 在 head 中添加字体 CSS
        String fontCss = "<style>\n" +
                "body { font-family: " + fontFamily + "; }\n" +
                "</style>\n";
        
        // 将字体 CSS 插入到 </head> 之前
        html = html.replace("</head>", fontCss + "</head>");
        
        return html;
    }

    /**
     * 加载系统字体
     */
    private void loadSystemFonts(PdfRendererBuilder builder) {
        String osName = System.getProperty("os.name").toLowerCase();
        java.util.List<String[]> fontPaths = new java.util.ArrayList<>();
        
        if (osName.contains("win")) {
            // Windows 字体路径
            fontPaths.add(new String[]{"C:/Windows/Fonts/simhei.ttf", "SimHei"});
            fontPaths.add(new String[]{"C:/Windows/Fonts/simsun.ttc", "SimSun"});
            fontPaths.add(new String[]{"C:/Windows/Fonts/msyh.ttc", "Microsoft YaHei"});
            fontPaths.add(new String[]{"C:/Windows/Fonts/msyhl.ttc", "Microsoft YaHei Light"});
        } else if (osName.contains("mac")) {
            // macOS 字体路径
            fontPaths.add(new String[]{"/System/Library/Fonts/PingFang.ttc", "PingFang SC"});
            fontPaths.add(new String[]{"/System/Library/Fonts/STHeiti Light.ttc", "STHeiti"});
            fontPaths.add(new String[]{"/System/Library/Fonts/Hiragino Sans GB.ttc", "Hiragino Sans GB"});
            fontPaths.add(new String[]{"/Library/Fonts/Arial Unicode.ttf", "Arial Unicode"});
        } else {
            // Linux 字体路径
            fontPaths.add(new String[]{"/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc", "WenQuanYi Zen Hei"});
            fontPaths.add(new String[]{"/usr/share/fonts/truetype/wqy/wqy-microhei.ttc", "WenQuanYi Micro Hei"});
            fontPaths.add(new String[]{"/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc", "Noto Sans CJK"});
            fontPaths.add(new String[]{"/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "DejaVu Sans"});
        }
        
        // 尝试加载字体
        for (String[] fontInfo : fontPaths) {
            try {
                java.io.File fontFile = new java.io.File(fontInfo[0]);
                if (fontFile.exists()) {
                    builder.useFont(fontFile, fontInfo[1]);
                    log.info("已加载字体: {}", fontInfo[1]);
                }
            } catch (Exception e) {
                log.debug("无法加载字体 {}: {}", fontInfo[1], e.getMessage());
            }
        }
    }

    /**
     * Markdown 转 Word
     */
    private String convertToWord(String markdown, java.util.List<String> enabledPlugins) throws Exception {
        String html = convertToHtml(markdown, enabledPlugins);

        // 创建 Word 文档
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
        XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordPackage);

        // 导入 HTML 内容
        wordPackage.getMainDocumentPart().getContent().addAll(
                xhtmlImporter.convert(html, null)
        );

        // 转换为 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Docx4J.save(wordPackage, baos);

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * 请求参数
     */
    @Data
    public static class ConverterRequest {
        /**
         * Markdown 内容
         */
        private String markdown;
        /**
         * 目标格式：html、pdf、word
         */
        private String targetFormat;
        /**
         * 启用的可选插件列表
         */
        private java.util.List<String> enabledPlugins;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ConverterResponse extends BaseToolResponse {
        /**
         * 转换结果（HTML 为文本，PDF/Word 为 Base64）
         */
        private String result;
        /**
         * 目标格式
         */
        private String targetFormat;
        /**
         * 内容类型
         */
        private String contentType;
        /**
         * 文件扩展名
         */
        private String fileExtension;
    }
}
