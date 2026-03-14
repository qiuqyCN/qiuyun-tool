package dev.qiuyun.qiuyuntoolbackend.executor.image;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Base64;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SvgEditorExecutor extends AbstractToolExecutor<SvgEditorExecutor.SvgEditorRequest, SvgEditorExecutor.SvgEditorResponse> {

    private static final Set<String> VALID_OPERATIONS = Set.of("optimize", "minify", "toPng", "format", "validate");
    private static final int MAX_SVG_SIZE = 10 * 1024 * 1024;
    private static final int MAX_PNG_WIDTH = 4096;
    private static final int MAX_PNG_HEIGHT = 4096;

    @Override
    public String getToolCode() {
        return "svg-editor";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(SvgEditorRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getSvgContent(), "SVG内容");
        validateNotEmpty(request.getOperation(), "操作类型");
        validateEnum(request.getOperation(), "操作类型", VALID_OPERATIONS);
        
        if (request.getSvgContent().length() > MAX_SVG_SIZE) {
            throw new BusinessException("SVG文件大小不能超过10MB");
        }
        
        if ("toPng".equals(request.getOperation())) {
            if (request.getWidth() != null && (request.getWidth() < 1 || request.getWidth() > MAX_PNG_WIDTH)) {
                throw new BusinessException("宽度必须在1到4096之间");
            }
            if (request.getHeight() != null && (request.getHeight() < 1 || request.getHeight() > MAX_PNG_HEIGHT)) {
                throw new BusinessException("高度必须在1到4096之间");
            }
        }
    }

    @Override
    protected SvgEditorResponse doExecute(SvgEditorRequest request, ToolContext context) throws Exception {
        String svgContent = request.getSvgContent();
        String operation = request.getOperation();
        
        SvgEditorResponse response = new SvgEditorResponse();
        response.setOperation(operation);
        
        switch (operation) {
            case "optimize":
            case "minify":
                String optimized = optimizeSvg(svgContent);
                response.setSvgContent(optimized);
                response.setOriginalSize(svgContent.length());
                response.setOptimizedSize(optimized.length());
                break;
                
            case "toPng":
                String pngBase64 = convertToPng(svgContent, request.getWidth(), request.getHeight());
                response.setPngBase64(pngBase64);
                response.setWidth(request.getWidth() != null ? request.getWidth() : 800);
                response.setHeight(request.getHeight() != null ? request.getHeight() : 600);
                break;
                
            case "format":
                String formatted = formatSvg(svgContent);
                response.setSvgContent(formatted);
                break;
                
            case "validate":
                ValidationResult result = validateSvg(svgContent);
                response.setValid(result.isValid());
                response.setValidationMessage(result.getMessage());
                response.setErrors(result.getErrors());
                break;
        }
        
        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "SVG处理失败: " + e.getMessage();
    }

    @Override
    public java.util.Map<String, Object> getToolConfig() {
        return java.util.Map.of(
                "name", "SVG编辑器",
                "description", "在线SVG编辑、优化、格式化和转换工具",
                "operations", java.util.Map.of(
                        "optimize", "优化SVG（移除冗余属性）",
                        "minify", "压缩SVG（移除空白和注释）",
                        "toPng", "转换为PNG图片",
                        "format", "格式化SVG代码",
                        "validate", "验证SVG有效性"
                ),
                "maxSize", MAX_SVG_SIZE,
                "maxPngSize", java.util.Map.of("width", MAX_PNG_WIDTH, "height", MAX_PNG_HEIGHT)
        );
    }

    private String optimizeSvg(String svgContent) throws Exception {
        Document document = parseSvg(svgContent);
        Element root = document.getDocumentElement();
        
        removeAttribute(root, "xmlns:xlink");
        removeAttribute(root, "xml:space");
        
        removeEmptyGroups(root);
        
        return documentToString(document, false);
    }

    private String formatSvg(String svgContent) throws Exception {
        Document document = parseSvg(svgContent);
        return documentToString(document, true);
    }

    private String convertToPng(String svgContent, Integer width, Integer height) throws Exception {
        StringReader reader = new StringReader(svgContent);
        TranscoderInput input = new TranscoderInput(reader);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);
        
        PNGTranscoder transcoder = new PNGTranscoder();
        if (width != null) {
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width.floatValue());
        }
        if (height != null) {
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height.floatValue());
        }
        
        transcoder.transcode(input, output);
        outputStream.flush();
        
        byte[] pngBytes = outputStream.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngBytes);
    }

    private ValidationResult validateSvg(String svgContent) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setErrors(new java.util.ArrayList<>());
        
        try {
            Document document = parseSvg(svgContent);
            Element root = document.getDocumentElement();
            
            if (!"svg".equals(root.getLocalName()) && !"svg".equals(root.getTagName())) {
                result.setValid(false);
                result.getErrors().add("根元素必须是<svg>");
            }
            
            if (!svgContent.contains("xmlns") && !root.hasAttribute("xmlns")) {
                result.getErrors().add("建议添加xmlns属性: xmlns=\"http://www.w3.org/2000/svg\"");
            }
            
            if (result.getErrors().isEmpty()) {
                result.setMessage("SVG格式正确");
            } else {
                result.setMessage("SVG格式存在警告");
            }
            
        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("SVG解析失败: " + e.getMessage());
            result.getErrors().add(e.getMessage());
        }
        
        return result;
    }

    private Document parseSvg(String svgContent) throws Exception {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(svgContent.getBytes("UTF-8"));
        return factory.createDocument(null, inputStream);
    }

    private String documentToString(Document document, boolean prettyPrint) throws Exception {
        Element root = document.getDocumentElement();
        String svgContent = elementToString(root, prettyPrint ? 0 : -1);
        
        if (prettyPrint) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + svgContent;
        } else {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + svgContent.replaceAll(">\\s+<", "><");
        }
    }

    private String elementToString(Element element, int indent) {
        StringBuilder sb = new StringBuilder();
        String tagName = element.getLocalName() != null ? element.getLocalName() : element.getTagName();
        
        if (indent >= 0) {
            sb.append("  ".repeat(indent));
        }
        
        sb.append("<").append(tagName);
        
        if (element.hasAttributes()) {
            for (int i = 0; i < element.getAttributes().getLength(); i++) {
                org.w3c.dom.Node attr = element.getAttributes().item(i);
                sb.append(" ").append(attr.getNodeName()).append("=\"").append(escapeXml(attr.getNodeValue())).append("\"");
            }
        }
        
        boolean hasChildren = element.hasChildNodes();
        boolean hasElementChildren = false;
        
        if (hasChildren) {
            org.w3c.dom.NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if (child instanceof Element) {
                    hasElementChildren = true;
                    break;
                }
            }
        }
        
        if (!hasChildren) {
            sb.append("/>");
        } else if (hasElementChildren) {
            sb.append(">");
            if (indent >= 0) sb.append("\n");
            
            org.w3c.dom.NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if (child instanceof Element) {
                    sb.append(elementToString((Element) child, indent >= 0 ? indent + 1 : -1));
                    if (indent >= 0) sb.append("\n");
                } else if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    String text = child.getTextContent().trim();
                    if (!text.isEmpty()) {
                        sb.append(escapeXml(text));
                    }
                }
            }
            
            if (indent >= 0) sb.append("  ".repeat(indent));
            sb.append("</").append(tagName).append(">");
        } else {
            sb.append(">");
            org.w3c.dom.NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    sb.append(escapeXml(child.getTextContent()));
                }
            }
            sb.append("</").append(tagName).append(">");
        }
        
        return sb.toString();
    }

    private void removeAttribute(Element element, String attrName) {
        element.removeAttribute(attrName);
        org.w3c.dom.NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child instanceof Element) {
                removeAttribute((Element) child, attrName);
            }
        }
    }

    private void removeEmptyGroups(Element element) {
        org.w3c.dom.NodeList children = element.getChildNodes();
        java.util.List<Element> toRemove = new java.util.ArrayList<>();
        
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                String tagName = childElement.getLocalName() != null ? childElement.getLocalName() : childElement.getTagName();
                
                if ("g".equals(tagName) && !childElement.hasChildNodes()) {
                    toRemove.add(childElement);
                } else {
                    removeEmptyGroups(childElement);
                }
            }
        }
        
        for (Element e : toRemove) {
            e.getParentNode().removeChild(e);
        }
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Data
    public static class SvgEditorRequest {
        private String svgContent;
        private String operation;
        private Integer width;
        private Integer height;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SvgEditorResponse extends BaseToolResponse {
        private String svgContent;
        private String pngBase64;
        private String operation;
        private Integer originalSize;
        private Integer optimizedSize;
        private Integer width;
        private Integer height;
        private Boolean valid;
        private String validationMessage;
        private java.util.List<String> errors;
    }

    @Data
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private java.util.List<String> errors;
    }
}
