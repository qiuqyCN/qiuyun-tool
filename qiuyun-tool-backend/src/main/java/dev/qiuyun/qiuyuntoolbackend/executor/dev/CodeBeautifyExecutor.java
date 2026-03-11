package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * 代码美化工具执行器
 * 支持：HTML、CSS、JavaScript、Java、SQL、XML 代码格式化
 */
@Slf4j
@Component
public class CodeBeautifyExecutor implements ToolExecutor<CodeBeautifyExecutor.CodeBeautifyRequest, CodeBeautifyExecutor.CodeBeautifyResponse> {

    @Override
    public String getToolCode() {
        return "code-beautify";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(CodeBeautifyRequest request) throws BusinessException {
        if (request == null || request.getInput() == null || request.getInput().trim().isEmpty()) {
            throw new BusinessException("输入内容不能为空");
        }
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            throw new BusinessException("语言类型不能为空");
        }
        // 验证语言类型
        if (!isValidLanguage(request.getLanguage())) {
            throw new BusinessException("不支持的语言类型: " + request.getLanguage());
        }
        // 验证操作类型
        if (request.getOperation() == null || request.getOperation().trim().isEmpty()) {
            throw new BusinessException("操作类型不能为空");
        }
        if (!"format".equals(request.getOperation()) && !"compress".equals(request.getOperation())) {
            throw new BusinessException("不支持的操作类型: " + request.getOperation());
        }
    }

    @Override
    public CodeBeautifyResponse execute(CodeBeautifyRequest request, ToolContext context) throws BusinessException {
        String input = request.getInput();
        String language = request.getLanguage();
        String operation = request.getOperation();

        try {
            String result;
            if ("format".equals(operation)) {
                result = formatCode(input, language);
            } else if ("compress".equals(operation)) {
                result = compressCode(input, language);
            } else {
                throw new BusinessException("不支持的操作类型: " + operation);
            }

            CodeBeautifyResponse response = new CodeBeautifyResponse();
            response.setSuccess(true);
            response.setResult(result);
            response.setLanguage(language);
            response.setOperation(operation);
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("代码处理错误: {}", e.getMessage());
            throw new BusinessException("处理失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "代码美化",
                "description", "HTML/CSS/JavaScript/Java/SQL/XML代码格式化",
                "languages", new String[]{"html", "css", "javascript", "java", "sql", "xml"},
                "languageLabels", Map.of(
                        "html", "HTML",
                        "css", "CSS",
                        "javascript", "JavaScript",
                        "java", "Java",
                        "sql", "SQL",
                        "xml", "XML"
                ),
                "operations", new String[]{"format", "compress"},
                "operationLabels", Map.of(
                        "format", "格式化",
                        "compress", "压缩"
                )
        );
    }

    private boolean isValidLanguage(String language) {
        return "html".equals(language) || "css".equals(language) || "javascript".equals(language)
                || "java".equals(language) || "sql".equals(language) || "xml".equals(language);
    }

    /**
     * 格式化代码
     */
    private String formatCode(String input, String language) throws Exception {
        switch (language) {
            case "html":
                return formatHtml(input);
            case "css":
                return formatCss(input);
            case "javascript":
                return formatJavaScript(input);
            case "java":
                return formatJava(input);
            case "sql":
                return formatSql(input);
            case "xml":
                return formatXml(input);
            default:
                throw new BusinessException("不支持的语言类型: " + language);
        }
    }

    /**
     * 压缩代码
     */
    private String compressCode(String input, String language) {
        switch (language) {
            case "html":
                return compressHtml(input);
            case "css":
                return compressCss(input);
            case "javascript":
                return compressJavaScript(input);
            case "java":
                return compressJava(input);
            case "sql":
                return compressSql(input);
            case "xml":
                return compressXml(input);
            default:
                throw new BusinessException("不支持的语言类型: " + language);
        }
    }

    // ==================== HTML (使用 JSoup) ====================

    /**
     * 使用 JSoup 格式化 HTML
     */
    private String formatHtml(String html) {
        try {
            Document doc = Jsoup.parse(html);
            doc.outputSettings()
                    .indentAmount(2)
                    .prettyPrint(true);
            return doc.html();
        } catch (Exception e) {
            log.error("HTML格式化错误: {}", e.getMessage());
            throw new BusinessException("HTML格式化失败: " + e.getMessage());
        }
    }

    /**
     * 压缩 HTML
     */
    private String compressHtml(String html) {
        return html
                .replaceAll("\\s+", " ")
                .replaceAll(">\\s+<", "><")
                .replaceAll("<!--.*?-->", "")
                .trim();
    }

    // ==================== CSS ====================

    /**
     * 格式化 CSS
     */
    private String formatCss(String css) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        char[] chars = css.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '{') {
                formatted.append(" {").append("\n");
                indentLevel++;
            } else if (c == '}') {
                indentLevel = Math.max(0, indentLevel - 1);
                formatted.append("\n").append(getIndent(indentLevel)).append("}").append("\n");
            } else if (c == ';') {
                formatted.append(";").append("\n");
            } else if (c == ':') {
                formatted.append(": ");
            } else if (!Character.isWhitespace(c)) {
                if (formatted.length() > 0 && formatted.charAt(formatted.length() - 1) == '\n') {
                    formatted.append(getIndent(indentLevel));
                }
                formatted.append(c);
            }
        }

        return formatted.toString().trim();
    }

    /**
     * 压缩 CSS
     */
    private String compressCss(String css) {
        return css
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*\\{\\s*", "{")
                .replaceAll("\\s*\\}\\s*", "}")
                .replaceAll("\\s*;\\s*", ";")
                .replaceAll("\\s*:\\s*", ":")
                .replaceAll("\\s*,\\s*", ",")
                .trim();
    }

    // ==================== JavaScript ====================

    /**
     * 格式化 JavaScript
     */
    private String formatJavaScript(String js) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inString = false;
        char stringChar = 0;
        boolean inComment = false;
        boolean inLineComment = false;
        char[] chars = js.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // 处理字符串
            if (!inComment && !inLineComment) {
                if ((c == '"' || c == '\'' || c == '`') && (i == 0 || chars[i - 1] != '\\')) {
                    if (!inString) {
                        inString = true;
                        stringChar = c;
                    } else if (c == stringChar) {
                        inString = false;
                    }
                }
            }

            // 处理注释
            if (!inString && !inComment && !inLineComment && i < chars.length - 1) {
                if (chars[i] == '/' && chars[i + 1] == '*') {
                    inComment = true;
                    formatted.append("/*");
                    i++;
                    continue;
                }
                if (chars[i] == '/' && chars[i + 1] == '/') {
                    inLineComment = true;
                    formatted.append("//");
                    i++;
                    continue;
                }
            }

            if (inComment && i > 0 && chars[i - 1] == '*' && c == '/') {
                inComment = false;
                formatted.append("/").append("\n").append(getIndent(indentLevel));
                continue;
            }

            if (inLineComment && c == '\n') {
                inLineComment = false;
                formatted.append("\n").append(getIndent(indentLevel));
                continue;
            }

            if (inString || inComment || inLineComment) {
                formatted.append(c);
                continue;
            }

            // 处理代码结构
            if (c == '{' || c == '[' || c == '(') {
                formatted.append(c);
                if (c == '{' || c == '[') {
                    indentLevel++;
                    formatted.append("\n").append(getIndent(indentLevel));
                }
            } else if (c == '}' || c == ']' || c == ')') {
                if (c == '}' || c == ']') {
                    indentLevel = Math.max(0, indentLevel - 1);
                    formatted.append("\n").append(getIndent(indentLevel));
                }
                formatted.append(c);
                if (c == '}') {
                    formatted.append("\n").append(getIndent(indentLevel));
                }
            } else if (c == ';') {
                formatted.append(";").append("\n").append(getIndent(indentLevel));
            } else if (c == '\n' || c == '\r') {
                // 跳过原始换行
            } else if (!Character.isWhitespace(c)) {
                formatted.append(c);
            } else if (formatted.length() > 0 && !Character.isWhitespace(formatted.charAt(formatted.length() - 1))) {
                formatted.append(' ');
            }
        }

        return formatted.toString().trim();
    }

    /**
     * 压缩 JavaScript
     */
    private String compressJavaScript(String js) {
        return js
                .replaceAll("/\\*.*?\\*/", "")
                .replaceAll("//.*?$", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*\\{\\s*", "{")
                .replaceAll("\\s*\\}\\s*", "}")
                .replaceAll("\\s*;\\s*", ";")
                .replaceAll("\\s*,\\s*", ",")
                .trim();
    }

    // ==================== Java (使用自定义格式化器) ====================

    /**
     * 格式化 Java 代码
     * 使用自定义格式化器避免 Google Java Format 的模块访问问题
     */
    private String formatJava(String java) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inString = false;
        char stringChar = 0;
        boolean inComment = false;
        boolean inLineComment = false;
        boolean inJavadoc = false;
        char[] chars = java.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // 处理字符串
            if (!inComment && !inLineComment && !inJavadoc) {
                if ((c == '"' || c == '\'') && (i == 0 || chars[i - 1] != '\\')) {
                    if (!inString) {
                        inString = true;
                        stringChar = c;
                    } else if (c == stringChar) {
                        inString = false;
                    }
                }
            }

            // 处理注释
            if (!inString && !inComment && !inLineComment && !inJavadoc && i < chars.length - 1) {
                if (chars[i] == '/' && chars[i + 1] == '*') {
                    // 检查是否是 Javadoc
                    if (i + 2 < chars.length && chars[i + 2] == '*') {
                        inJavadoc = true;
                        formatted.append("/**");
                        i += 2;
                    } else {
                        inComment = true;
                        formatted.append("/*");
                        i++;
                    }
                    continue;
                }
                if (chars[i] == '/' && chars[i + 1] == '/') {
                    inLineComment = true;
                    formatted.append("//");
                    i++;
                    continue;
                }
            }

            if ((inComment || inJavadoc) && i > 0 && chars[i - 1] == '*' && c == '/') {
                if (inJavadoc) {
                    inJavadoc = false;
                    formatted.append("/").append("\n").append(getIndent(indentLevel));
                } else {
                    inComment = false;
                    formatted.append("/").append("\n").append(getIndent(indentLevel));
                }
                continue;
            }

            if (inLineComment && c == '\n') {
                inLineComment = false;
                formatted.append("\n").append(getIndent(indentLevel));
                continue;
            }

            if (inString || inComment || inLineComment || inJavadoc) {
                formatted.append(c);
                continue;
            }

            // 处理代码结构
            if (c == '{' || c == '[') {
                formatted.append(c);
                indentLevel++;
                formatted.append("\n").append(getIndent(indentLevel));
            } else if (c == '}' || c == ']') {
                indentLevel = Math.max(0, indentLevel - 1);
                formatted.append("\n").append(getIndent(indentLevel)).append(c);
            } else if (c == '(') {
                formatted.append(c);
            } else if (c == ')') {
                formatted.append(c);
            } else if (c == ';') {
                formatted.append(";").append("\n").append(getIndent(indentLevel));
            } else if (c == '\n' || c == '\r') {
                // 跳过原始换行
            } else if (!Character.isWhitespace(c)) {
                formatted.append(c);
            } else if (formatted.length() > 0 && !Character.isWhitespace(formatted.charAt(formatted.length() - 1))) {
                formatted.append(' ');
            }
        }

        return formatted.toString().trim();
    }

    /**
     * 压缩 Java 代码（移除多余空白和注释）
     */
    private String compressJava(String java) {
        return java
                .replaceAll("/\\*\\*.*?\\*/", "")  // 移除文档注释
                .replaceAll("/\\*.*?\\*/", "")      // 移除多行注释
                .replaceAll("//.*?$", "")           // 移除单行注释
                .replaceAll("\\s+", " ")            // 多个空白替换为单个
                .trim();
    }

    // ==================== SQL (使用 sqlformatter) ====================

    /**
     * 使用 sqlformatter 格式化 SQL
     */
    private String formatSql(String sql) {
        FormatConfig config = FormatConfig.builder()
                .indent("  ")
                .build();
        return SqlFormatter.format(sql, config);
    }

    /**
     * 压缩 SQL
     */
    private String compressSql(String sql) {
        return sql
                .replaceAll("--.*?$", "")           // 移除单行注释
                .replaceAll("/\\*.*?\\*/", "")      // 移除多行注释
                .replaceAll("\\s+", " ")            // 多个空白替换为单个
                .trim();
    }

    // ==================== XML (使用 Transformer) ====================

    /**
     * 使用 Transformer 格式化 XML
     */
    private String formatXml(String xml) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        StringWriter writer = new StringWriter();
        transformer.transform(
                new StreamSource(new StringReader(xml)),
                new StreamResult(writer)
        );
        return writer.toString();
    }

    /**
     * 压缩 XML
     */
    private String compressXml(String xml) {
        return xml
                .replaceAll("<!--.*?-->", "")       // 移除注释
                .replaceAll(">\\s+<", "><")         // 标签间的空白
                .replaceAll("\\s+", " ")             // 多个空白替换为单个
                .trim();
    }

    // ==================== 工具方法 ====================

    private String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  "); // 2个空格缩进
        }
        return sb.toString();
    }

    /**
     * 请求参数
     */
    @Data
    public static class CodeBeautifyRequest {
        /**
         * 语言类型：html、css、javascript、java、sql、xml
         */
        private String language;
        /**
         * 操作类型：format(格式化)、compress(压缩)
         */
        private String operation;
        /**
         * 输入的代码
         */
        private String input;
    }

    /**
     * 响应结果
     */
    @Data
    public static class CodeBeautifyResponse {
        /**
         * 是否成功
         */
        private boolean success;
        /**
         * 处理结果
         */
        private String result;
        /**
         * 语言类型
         */
        private String language;
        /**
         * 操作类型
         */
        private String operation;
        /**
         * 错误信息（失败时）
         */
        private String errorMessage;
    }
}
