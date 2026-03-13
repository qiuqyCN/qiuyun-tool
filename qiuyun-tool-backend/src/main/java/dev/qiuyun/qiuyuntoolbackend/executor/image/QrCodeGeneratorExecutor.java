package dev.qiuyun.qiuyuntoolbackend.executor.image;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
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
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 二维码生成器执行器
 * 支持：文本、URL、WiFi、邮箱、电话等类型二维码生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeGeneratorExecutor extends AbstractToolExecutor<QrCodeGeneratorExecutor.GeneratorRequest, QrCodeGeneratorExecutor.GeneratorResponse> {

    private static final Set<String> VALID_CONTENT_TYPES = Set.of("text", "url", "wifi", "email", "phone", "sms");
    private static final Set<String> VALID_ERROR_CORRECTIONS = Set.of("L", "M", "Q", "H");
    private static final int MIN_SIZE = 100;
    private static final int MAX_SIZE = 1000;

    @Override
    public String getToolCode() {
        return "qr-code-generator";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(GeneratorRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getContent(), "二维码内容");
        validateNotEmpty(request.getContentType(), "内容类型");
        validateEnum(request.getContentType(), "内容类型", VALID_CONTENT_TYPES);
        
        if (request.getSize() != null) {
            validateRange(request.getSize(), "尺寸", MIN_SIZE, MAX_SIZE);
        }
        
        if (request.getErrorCorrection() != null) {
            validateEnum(request.getErrorCorrection(), "纠错级别", VALID_ERROR_CORRECTIONS);
        }
    }

    @Override
    protected GeneratorResponse doExecute(GeneratorRequest request, ToolContext context) throws Exception {
        String content = formatContent(request);
        int size = request.getSize() != null ? request.getSize() : 300;
        String errorCorrection = request.getErrorCorrection() != null ? request.getErrorCorrection() : "M";
        
        // 生成二维码
        BufferedImage qrImage = generateQRCode(content, size, errorCorrection);
        
        // 如果有Logo，添加Logo
        if (request.getLogoBase64() != null && !request.getLogoBase64().isEmpty()) {
            qrImage = addLogo(qrImage, request.getLogoBase64());
        }
        
        // 转换为Base64
        String base64Image = imageToBase64(qrImage);
        
        GeneratorResponse response = new GeneratorResponse();
        response.setSuccess(true);
        response.setQrCodeBase64(base64Image);
        response.setSize(size);
        response.setContentType(request.getContentType());
        
        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "二维码生成失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "二维码生成",
                "description", "生成各种类型的二维码，支持自定义样式和Logo",
                "contentTypes", Map.of(
                        "text", "纯文本",
                        "url", "网址链接",
                        "wifi", "WiFi连接",
                        "email", "电子邮箱",
                        "phone", "电话号码",
                        "sms", "短信"
                ),
                "errorCorrectionLevels", Map.of(
                        "L", "低 (~7%)",
                        "M", "中 (~15%)",
                        "Q", "高 (~25%)",
                        "H", "最高 (~30%)"
                ),
                "defaultSize", 300,
                "sizeRange", Map.of("min", MIN_SIZE, "max", MAX_SIZE),
                "supportsLogo", true,
                "supportsColor", true
        );
    }

    /**
     * 格式化内容
     */
    private String formatContent(GeneratorRequest request) {
        String content = request.getContent();
        String type = request.getContentType();
        
        switch (type) {
            case "url":
                // 确保URL有协议前缀
                if (!content.startsWith("http://") && !content.startsWith("https://")) {
                    content = "https://" + content;
                }
                return content;
                
            case "wifi":
                // WiFi格式: WIFI:S:ssid;T:WPA;P:password;H:false;;
                String ssid = request.getWifiSsid() != null ? request.getWifiSsid() : content;
                String password = request.getWifiPassword() != null ? request.getWifiPassword() : "";
                String encryption = request.getWifiEncryption() != null ? request.getWifiEncryption() : "WPA";
                boolean hidden = request.getWifiHidden() != null ? request.getWifiHidden() : false;
                return String.format("WIFI:S:%s;T:%s;P:%s;H:%s;;", 
                        escapeSpecialChars(ssid), 
                        encryption, 
                        escapeSpecialChars(password),
                        hidden);
                        
            case "email":
                // 邮件格式: mailto:email@example.com?subject=xxx&body=xxx
                String emailSubject = request.getEmailSubject() != null ? request.getEmailSubject() : "";
                String emailBody = request.getEmailBody() != null ? request.getEmailBody() : "";
                StringBuilder mailto = new StringBuilder("mailto:").append(content);
                if (!emailSubject.isEmpty() || !emailBody.isEmpty()) {
                    mailto.append("?");
                    if (!emailSubject.isEmpty()) {
                        mailto.append("subject=").append(urlEncode(emailSubject));
                    }
                    if (!emailBody.isEmpty()) {
                        if (!emailSubject.isEmpty()) mailto.append("&");
                        mailto.append("body=").append(urlEncode(emailBody));
                    }
                }
                return mailto.toString();
                
            case "phone":
                // 电话格式: tel:+1234567890
                return "tel:" + content.replaceAll("[^0-9+]", "");
                
            case "sms":
                // 短信格式: smsto:+1234567890:消息内容
                String smsBody = request.getSmsBody() != null ? request.getSmsBody() : "";
                return String.format("smsto:%s:%s", content.replaceAll("[^0-9+]", ""), smsBody);
                
            default:
                return content;
        }
    }

    /**
     * 生成二维码图片
     */
    private BufferedImage generateQRCode(String content, int size, String errorCorrection) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.valueOf(errorCorrection));
        hints.put(EncodeHintType.MARGIN, 1);
        
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 添加Logo到二维码
     */
    private BufferedImage addLogo(BufferedImage qrImage, String logoBase64) throws IOException {
        // 解码Logo
        byte[] logoBytes = Base64.getDecoder().decode(logoBase64.split(",")[1]);
        BufferedImage logoImage = ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));
        
        if (logoImage == null) {
            return qrImage;
        }
        
        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();
        
        // Logo大小为二维码的1/5
        int logoSize = qrWidth / 5;
        
        // 创建新的图片
        BufferedImage combined = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();
        
        // 绘制二维码
        g.drawImage(qrImage, 0, 0, null);
        
        // 计算Logo位置
        int x = (qrWidth - logoSize) / 2;
        int y = (qrHeight - logoSize) / 2;
        
        // 绘制白色背景圆角矩形
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(x - 5, y - 5, logoSize + 10, logoSize + 10, 10, 10));
        
        // 绘制Logo
        g.drawImage(logoImage.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH), x, y, null);
        
        g.dispose();
        
        return combined;
    }

    /**
     * 图片转Base64
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 转义特殊字符
     */
    private String escapeSpecialChars(String input) {
        return input.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(":", "\\:")
                .replace(",", "\\,");
    }

    /**
     * URL编码
     */
    private String urlEncode(String input) {
        return java.net.URLEncoder.encode(input, java.nio.charset.StandardCharsets.UTF_8);
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class GeneratorRequest {
        /**
         * 二维码内容
         */
        private String content;
        /**
         * 内容类型：text, url, wifi, email, phone, sms
         */
        private String contentType;
        /**
         * 尺寸（像素），默认300
         */
        private Integer size;
        /**
         * 纠错级别：L, M, Q, H，默认M
         */
        private String errorCorrection;
        /**
         * Logo图片Base64（可选）
         */
        private String logoBase64;
        
        // WiFi专用字段
        private String wifiSsid;
        private String wifiPassword;
        private String wifiEncryption;
        private Boolean wifiHidden;
        
        // 邮件专用字段
        private String emailSubject;
        private String emailBody;
        
        // 短信专用字段
        private String smsBody;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GeneratorResponse extends BaseToolResponse {
        /**
         * 二维码图片Base64
         */
        private String qrCodeBase64;
        /**
         * 尺寸
         */
        private int size;
        /**
         * 内容类型
         */
        private String contentType;
    }
}
