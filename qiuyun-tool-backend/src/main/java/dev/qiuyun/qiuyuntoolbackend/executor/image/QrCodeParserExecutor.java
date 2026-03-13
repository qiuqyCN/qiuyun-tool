package dev.qiuyun.qiuyuntoolbackend.executor.image;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 二维码解析器执行器
 * 支持：解析二维码图片，识别内容类型，提取结构化数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeParserExecutor extends AbstractToolExecutor<QrCodeParserExecutor.ParserRequest, QrCodeParserExecutor.ParserResponse> {

    @Override
    public String getToolCode() {
        return "qr-code-parser";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ParserRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getImageBase64(), "二维码图片");
    }

    @Override
    protected ParserResponse doExecute(ParserRequest request, ToolContext context) throws Exception {
        String imageBase64 = request.getImageBase64();
        
        // 解码图片
        BufferedImage image = decodeBase64Image(imageBase64);
        if (image == null) {
            throw new BusinessException("无法读取图片，请确保上传的是有效的图片文件");
        }
        
        // 解析二维码
        String rawContent = decodeQRCode(image);
        if (rawContent == null) {
            throw new BusinessException("未能识别到二维码，请确保图片中包含清晰的二维码");
        }
        
        // 解析内容类型和结构化数据
        ContentType contentType = detectContentType(rawContent);
        Map<String, Object> structuredData = parseStructuredData(rawContent, contentType);
        
        ParserResponse response = new ParserResponse();
        response.setSuccess(true);
        response.setRawContent(rawContent);
        response.setContentType(contentType.getType());
        response.setContentTypeLabel(contentType.getLabel());
        response.setStructuredData(structuredData);
        
        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "二维码解析失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "二维码解析",
                "description", "上传二维码图片，解析其中的内容信息",
                "supportedFormats", new String[]{"PNG", "JPG", "JPEG", "GIF", "BMP", "WEBP"},
                "supportedTypes", Map.of(
                        "text", "纯文本",
                        "url", "网址链接",
                        "wifi", "WiFi连接",
                        "email", "电子邮箱",
                        "phone", "电话号码",
                        "sms", "短信",
                        "vcard", "联系人卡片",
                        "geo", "地理位置"
                )
        );
    }

    /**
     * 解码Base64图片
     */
    private BufferedImage decodeBase64Image(String base64Image) throws IOException {
        // 移除data URI scheme前缀
        String base64Data = base64Image;
        if (base64Image.contains(",")) {
            base64Data = base64Image.split(",")[1];
        }
        
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    /**
     * 解码二维码
     */
    private String decodeQRCode(BufferedImage image) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        // 支持多种条码格式
        hints.put(DecodeHintType.POSSIBLE_FORMATS, java.util.Arrays.asList(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.PDF_417,
            BarcodeFormat.AZTEC
        ));
        
        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

    /**
     * 检测内容类型
     */
    private ContentType detectContentType(String content) {
        if (content == null || content.isEmpty()) {
            return ContentType.TEXT;
        }
        
        String lowerContent = content.toLowerCase();
        
        // WiFi
        if (lowerContent.startsWith("wifi:")) {
            return ContentType.WIFI;
        }
        
        // URL
        if (lowerContent.startsWith("http://") || lowerContent.startsWith("https://")) {
            return ContentType.URL;
        }
        
        // Email
        if (lowerContent.startsWith("mailto:")) {
            return ContentType.EMAIL;
        }
        
        // Phone
        if (lowerContent.startsWith("tel:")) {
            return ContentType.PHONE;
        }
        
        // SMS
        if (lowerContent.startsWith("smsto:") || lowerContent.startsWith("sms:")) {
            return ContentType.SMS;
        }
        
        // vCard
        if (lowerContent.startsWith("begin:vcard")) {
            return ContentType.VCARD;
        }
        
        // Geo location
        if (lowerContent.startsWith("geo:")) {
            return ContentType.GEO;
        }
        
        return ContentType.TEXT;
    }

    /**
     * 解析结构化数据
     */
    private Map<String, Object> parseStructuredData(String content, ContentType type) {
        Map<String, Object> data = new HashMap<>();
        
        switch (type) {
            case WIFI:
                parseWifiData(content, data);
                break;
            case EMAIL:
                parseEmailData(content, data);
                break;
            case PHONE:
                parsePhoneData(content, data);
                break;
            case SMS:
                parseSmsData(content, data);
                break;
            case VCARD:
                parseVCardData(content, data);
                break;
            case GEO:
                parseGeoData(content, data);
                break;
            case URL:
                data.put("url", content);
                data.put("displayUrl", truncateUrl(content, 60));
                break;
            default:
                data.put("text", content);
        }
        
        return data;
    }

    /**
     * 解析WiFi数据
     * 格式: WIFI:S:ssid;T:WPA;P:password;H:false;;
     */
    private void parseWifiData(String content, Map<String, Object> data) {
        data.put("ssid", extractWifiParam(content, "S"));
        data.put("encryption", extractWifiParam(content, "T"));
        data.put("password", extractWifiParam(content, "P"));
        String hidden = extractWifiParam(content, "H");
        data.put("hidden", "true".equalsIgnoreCase(hidden));
    }

    private String extractWifiParam(String content, String param) {
        Pattern pattern = Pattern.compile(param + ":([^;]+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String value = matcher.group(1);
            // 处理转义字符
            return value.replace("\\;", ";").replace("\\:", ":").replace("\\\\", "\\");
        }
        return "";
    }

    /**
     * 解析邮件数据
     * 格式: mailto:email@example.com?subject=xxx&body=xxx
     */
    private void parseEmailData(String content, Map<String, Object> data) {
        // 移除 mailto: 前缀
        String emailPart = content.substring(7);
        
        String email;
        String subject = "";
        String body = "";
        
        int queryIndex = emailPart.indexOf('?');
        if (queryIndex > 0) {
            email = emailPart.substring(0, queryIndex);
            String query = emailPart.substring(queryIndex + 1);
            
            // 解析查询参数
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    if ("subject".equalsIgnoreCase(keyValue[0])) {
                        subject = value;
                    } else if ("body".equalsIgnoreCase(keyValue[0])) {
                        body = value;
                    }
                }
            }
        } else {
            email = emailPart;
        }
        
        data.put("email", email);
        data.put("subject", subject);
        data.put("body", body);
    }

    /**
     * 解析电话数据
     * 格式: tel:+1234567890
     */
    private void parsePhoneData(String content, Map<String, Object> data) {
        String phone = content.substring(4); // 移除 tel:
        data.put("phone", phone);
    }

    /**
     * 解析短信数据
     * 格式: smsto:+1234567890:消息内容
     */
    private void parseSmsData(String content, Map<String, Object> data) {
        String smsPart = content.startsWith("smsto:") ? content.substring(6) : content.substring(4);
        
        int colonIndex = smsPart.indexOf(':');
        if (colonIndex > 0) {
            data.put("phone", smsPart.substring(0, colonIndex));
            data.put("message", smsPart.substring(colonIndex + 1));
        } else {
            data.put("phone", smsPart);
            data.put("message", "");
        }
    }

    /**
     * 解析vCard数据
     */
    private void parseVCardData(String content, Map<String, Object> data) {
        String[] lines = content.split("\r?\n");
        for (String line : lines) {
            if (line.startsWith("FN:") || line.startsWith("FN;")) {
                data.put("fullName", extractVCardValue(line));
            } else if (line.startsWith("TEL") || line.startsWith("TEL;")) {
                data.put("phone", extractVCardValue(line));
            } else if (line.startsWith("EMAIL") || line.startsWith("EMAIL;")) {
                data.put("email", extractVCardValue(line));
            } else if (line.startsWith("ORG") || line.startsWith("ORG;")) {
                data.put("organization", extractVCardValue(line));
            } else if (line.startsWith("TITLE") || line.startsWith("TITLE;")) {
                data.put("title", extractVCardValue(line));
            }
        }
    }

    private String extractVCardValue(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex > 0) {
            return line.substring(colonIndex + 1);
        }
        return "";
    }

    /**
     * 解析地理位置数据
     * 格式: geo:latitude,longitude?q=query
     */
    private void parseGeoData(String content, Map<String, Object> data) {
        String geoPart = content.substring(5); // 移除 geo:
        
        int queryIndex = geoPart.indexOf('?');
        String coords = queryIndex > 0 ? geoPart.substring(0, queryIndex) : geoPart;
        
        String[] parts = coords.split(",");
        if (parts.length >= 2) {
            data.put("latitude", parts[0]);
            data.put("longitude", parts[1]);
        }
        
        if (queryIndex > 0) {
            String query = geoPart.substring(queryIndex + 1);
            if (query.startsWith("q=")) {
                data.put("query", URLDecoder.decode(query.substring(2), StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * 截断URL用于显示
     */
    private String truncateUrl(String url, int maxLength) {
        if (url.length() <= maxLength) {
            return url;
        }
        return url.substring(0, maxLength - 3) + "...";
    }

    // ==================== 内容类型枚举 ====================

    private enum ContentType {
        TEXT("text", "纯文本"),
        URL("url", "网址链接"),
        WIFI("wifi", "WiFi连接"),
        EMAIL("email", "电子邮箱"),
        PHONE("phone", "电话号码"),
        SMS("sms", "短信"),
        VCARD("vcard", "联系人卡片"),
        GEO("geo", "地理位置");

        private final String type;
        private final String label;

        ContentType(String type, String label) {
            this.type = type;
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public String getLabel() {
            return label;
        }
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class ParserRequest {
        /**
         * 二维码图片Base64
         */
        private String imageBase64;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ParserResponse extends BaseToolResponse {
        /**
         * 原始内容
         */
        private String rawContent;
        /**
         * 内容类型
         */
        private String contentType;
        /**
         * 内容类型标签
         */
        private String contentTypeLabel;
        /**
         * 结构化数据
         */
        private Map<String, Object> structuredData;
    }
}
