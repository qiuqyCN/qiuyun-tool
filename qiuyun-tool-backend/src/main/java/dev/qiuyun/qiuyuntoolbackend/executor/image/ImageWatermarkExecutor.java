package dev.qiuyun.qiuyuntoolbackend.executor.image;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 图片水印工具执行器
 * 支持：文字水印、图片水印、九宫格位置、平铺模式、透明度、旋转
 */
@Slf4j
@Component
public class ImageWatermarkExecutor extends AbstractToolExecutor<ImageWatermarkExecutor.ImageWatermarkRequest, ImageWatermarkExecutor.ImageWatermarkResponse> {

    private static final Set<String> VALID_FORMATS = Set.of("jpeg", "jpg", "png", "webp", "bmp", "gif");
    private static final Set<String> VALID_POSITIONS = Set.of(
            "top-left", "top-center", "top-right",
            "center-left", "center", "center-right",
            "bottom-left", "bottom-center", "bottom-right",
            "tile"
    );
    private static final Set<String> VALID_FONT_FAMILIES = Set.of(
            "Microsoft YaHei", "SimHei", "SimSun", "KaiTi", "FangSong",
            "Arial", "Helvetica", "Times New Roman", "Georgia", "Verdana"
    );

    @Override
    public String getToolCode() {
        return "image-watermark";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ImageWatermarkRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getImageData(), "图片数据");
        validateNotEmpty(request.getWatermarkType(), "水印类型");
        validateNotEmpty(request.getPosition(), "水印位置");

        // 验证水印类型
        if (!"text".equals(request.getWatermarkType()) && !"image".equals(request.getWatermarkType())) {
            throw new BusinessException("水印类型必须是 text 或 image");
        }

        // 验证位置
        validateEnum(request.getPosition(), "水印位置", VALID_POSITIONS);

        // 验证透明度
        validateRange(request.getOpacity(), "透明度", 0, 100);

        // 验证旋转角度
        validateRange(request.getRotation(), "旋转角度", 0, 360);

        // 验证边距
        validateRange(request.getMarginX(), "水平边距", 0, 500);
        validateRange(request.getMarginY(), "垂直边距", 0, 500);

        // 文字水印特有验证
        if ("text".equals(request.getWatermarkType())) {
            validateNotEmpty(request.getText(), "水印文字");
            if (request.getText().length() > 100) {
                throw new BusinessException("水印文字不能超过100个字符");
            }
            if (request.getFontSize() != null) {
                validateRange(request.getFontSize(), "字号", 12, 200);
            }
            if (request.getFontFamily() != null) {
                validateEnum(request.getFontFamily(), "字体", VALID_FONT_FAMILIES);
            }
        }

        // 图片水印特有验证
        if ("image".equals(request.getWatermarkType())) {
            validateNotEmpty(request.getWatermarkImageData(), "水印图片数据");
            if (request.getWatermarkScale() != null) {
                validateRange(request.getWatermarkScale(), "缩放比例", 10, 100);
            }
        }

        // 验证输出格式
        if (!"original".equals(request.getOutputFormat())) {
            validateEnum(request.getOutputFormat(), "输出格式", VALID_FORMATS);
        }

        // 验证质量
        if (request.getQuality() != null) {
            validateRange(request.getQuality(), "输出质量", 1, 100);
        }
    }

    @Override
    protected ImageWatermarkResponse doExecute(ImageWatermarkRequest request, ToolContext context) throws Exception {
        // 解码原图
        byte[] imageBytes = Base64.getDecoder().decode(request.getImageData());
        BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (sourceImage == null) {
            throw new BusinessException("无法读取图片数据");
        }

        // 转换颜色空间（处理CMYK等特殊颜色空间）
        sourceImage = convertToRGB(sourceImage);

        int originalWidth = sourceImage.getWidth();
        int originalHeight = sourceImage.getHeight();
        long originalSize = imageBytes.length;

        // 确定输出格式
        String targetFormat = request.getOutputFormat().equals("original")
                ? detectFormat(request.getImageData())
                : request.getOutputFormat();
        if (targetFormat.equals("jpg")) {
            targetFormat = "jpeg";
        }

        // 创建带透明通道的画布
        BufferedImage canvas = new BufferedImage(
                originalWidth,
                originalHeight,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = canvas.createGraphics();

        // 绘制原图
        g2d.drawImage(sourceImage, 0, 0, null);

        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // 设置透明度
        float alpha = request.getOpacity() / 100f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // 根据水印类型绘制
        if ("text".equals(request.getWatermarkType())) {
            drawTextWatermark(g2d, request, originalWidth, originalHeight);
        } else {
            drawImageWatermark(g2d, request, originalWidth, originalHeight);
        }

        g2d.dispose();

        // 输出图片
        byte[] outputBytes = encodeImage(canvas, targetFormat, request.getQuality());
        String watermarkedData = Base64.getEncoder().encodeToString(outputBytes);

        // 构建响应
        ImageWatermarkResponse response = new ImageWatermarkResponse();
        response.setSuccess(true);
        response.setOriginalData(request.getImageData());
        response.setWatermarkedData(watermarkedData);
        response.setOriginalSize(originalSize);
        response.setWatermarkedSize(outputBytes.length);
        response.setOriginalWidth(originalWidth);
        response.setOriginalHeight(originalHeight);
        response.setFormat(targetFormat);

        return response;
    }

    /**
     * 绘制文字水印
     */
    private void drawTextWatermark(Graphics2D g2d, ImageWatermarkRequest request,
                                   int imgWidth, int imgHeight) {
        // 设置字体
        String fontFamily = request.getFontFamily() != null ? request.getFontFamily() : "Microsoft YaHei";
        int fontSize = request.getFontSize() != null ? request.getFontSize() : 48;
        int fontStyle = "bold".equals(request.getFontWeight()) ? Font.BOLD : Font.PLAIN;
        Font font = new Font(fontFamily, fontStyle, fontSize);
        g2d.setFont(font);

        // 设置颜色
        String colorHex = request.getFontColor() != null ? request.getFontColor() : "#000000";
        g2d.setColor(Color.decode(colorHex));

        // 计算文字尺寸
        FontMetrics fm = g2d.getFontMetrics();
        String text = request.getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int ascent = fm.getAscent();

        // 计算位置
        int marginX = request.getMarginX();
        int marginY = request.getMarginY();

        if ("tile".equals(request.getPosition())) {
            // 平铺模式
            drawTiledText(g2d, text, textWidth, textHeight, imgWidth, imgHeight,
                    request.getRotation(), marginX, marginY);
        } else {
            // 单位置模式
            Point position = calculatePosition(request.getPosition(), imgWidth, imgHeight,
                    textWidth, textHeight, marginX, marginY);

            // 应用旋转
            if (request.getRotation() != 0) {
                AffineTransform originalTransform = g2d.getTransform();
                g2d.rotate(Math.toRadians(request.getRotation()),
                        position.x + textWidth / 2.0,
                        position.y + textHeight / 2.0);
                g2d.drawString(text, position.x, position.y + ascent);
                g2d.setTransform(originalTransform);
            } else {
                g2d.drawString(text, position.x, position.y + ascent);
            }
        }
    }

    /**
     * 绘制图片水印
     */
    private void drawImageWatermark(Graphics2D g2d, ImageWatermarkRequest request,
                                    int imgWidth, int imgHeight) throws IOException {
        // 解码水印图片
        byte[] watermarkBytes = Base64.getDecoder().decode(request.getWatermarkImageData());
        BufferedImage watermarkImage = ImageIO.read(new ByteArrayInputStream(watermarkBytes));
        if (watermarkImage == null) {
            throw new BusinessException("无法读取水印图片");
        }

        // 计算缩放后尺寸
        int scale = request.getWatermarkScale() != null ? request.getWatermarkScale() : 30;
        int wmWidth = (int) (watermarkImage.getWidth() * scale / 100.0);
        int wmHeight = (int) (watermarkImage.getHeight() * scale / 100.0);

        int marginX = request.getMarginX();
        int marginY = request.getMarginY();

        if ("tile".equals(request.getPosition())) {
            // 平铺模式
            drawTiledImage(g2d, watermarkImage, wmWidth, wmHeight, imgWidth, imgHeight,
                    request.getRotation(), marginX, marginY);
        } else {
            // 单位置模式
            Point position = calculatePosition(request.getPosition(), imgWidth, imgHeight,
                    wmWidth, wmHeight, marginX, marginY);

            // 应用旋转并绘制
            drawRotatedImage(g2d, watermarkImage, position.x, position.y, wmWidth, wmHeight,
                    request.getRotation());
        }
    }

    /**
     * 平铺文字
     */
    private void drawTiledText(Graphics2D g2d, String text, int textWidth, int textHeight,
                               int imgWidth, int imgHeight, int rotation, int marginX, int marginY) {
        FontMetrics fm = g2d.getFontMetrics();
        int ascent = fm.getAscent();

        // 计算间距
        int spacingX = textWidth + Math.max(marginX, 50);
        int spacingY = textHeight + Math.max(marginY, 30);

        // 交错偏移
        int staggerOffset = spacingX / 2;

        for (int y = -textHeight; y < imgHeight + textHeight; y += spacingY) {
            int rowOffset = ((y / spacingY) % 2 == 0) ? 0 : staggerOffset;
            for (int x = -textWidth; x < imgWidth + textWidth; x += spacingX) {
                int drawX = x + rowOffset;
                int drawY = y;

                if (rotation != 0) {
                    AffineTransform originalTransform = g2d.getTransform();
                    g2d.rotate(Math.toRadians(rotation), drawX + textWidth / 2.0, drawY + textHeight / 2.0);
                    g2d.drawString(text, drawX, drawY + ascent);
                    g2d.setTransform(originalTransform);
                } else {
                    g2d.drawString(text, drawX, drawY + ascent);
                }
            }
        }
    }

    /**
     * 平铺图片
     */
    private void drawTiledImage(Graphics2D g2d, BufferedImage watermark,
                                int wmWidth, int wmHeight, int imgWidth, int imgHeight,
                                int rotation, int marginX, int marginY) {
        int spacingX = wmWidth + Math.max(marginX, 50);
        int spacingY = wmHeight + Math.max(marginY, 30);
        int staggerOffset = spacingX / 2;

        for (int y = -wmHeight; y < imgHeight + wmHeight; y += spacingY) {
            int rowOffset = ((y / spacingY) % 2 == 0) ? 0 : staggerOffset;
            for (int x = -wmWidth; x < imgWidth + wmWidth; x += spacingX) {
                int drawX = x + rowOffset;
                int drawY = y;
                drawRotatedImage(g2d, watermark, drawX, drawY, wmWidth, wmHeight, rotation);
            }
        }
    }

    /**
     * 绘制旋转的图片
     */
    private void drawRotatedImage(Graphics2D g2d, BufferedImage image,
                                  int x, int y, int width, int height, int rotation) {
        if (rotation != 0) {
            AffineTransform originalTransform = g2d.getTransform();
            g2d.rotate(Math.toRadians(rotation), x + width / 2.0, y + height / 2.0);
            g2d.drawImage(image, x, y, width, height, null);
            g2d.setTransform(originalTransform);
        } else {
            g2d.drawImage(image, x, y, width, height, null);
        }
    }

    /**
     * 计算水印位置
     */
    private Point calculatePosition(String position, int imgW, int imgH,
                                    int wmW, int wmH, int marginX, int marginY) {
        int x = marginX;
        int y = marginY;

        switch (position) {
            case "top-center":
                x = (imgW - wmW) / 2;
                break;
            case "top-right":
                x = imgW - wmW - marginX;
                break;
            case "center-left":
                x = marginX;
                y = (imgH - wmH) / 2;
                break;
            case "center":
                x = (imgW - wmW) / 2;
                y = (imgH - wmH) / 2;
                break;
            case "center-right":
                x = imgW - wmW - marginX;
                y = (imgH - wmH) / 2;
                break;
            case "bottom-left":
                x = marginX;
                y = imgH - wmH - marginY;
                break;
            case "bottom-center":
                x = (imgW - wmW) / 2;
                y = imgH - wmH - marginY;
                break;
            case "bottom-right":
                x = imgW - wmW - marginX;
                y = imgH - wmH - marginY;
                break;
            case "top-left":
            default:
                x = marginX;
                y = marginY;
                break;
        }

        return new Point(x, y);
    }

    /**
     * 编码图片
     */
    private byte[] encodeImage(BufferedImage image, String format, Integer quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (format.equals("png")) {
            ImageIO.write(image, "png", baos);
        } else {
            // JPEG 和 WebP 使用质量压缩
            // 对于JPEG，需要先移除透明通道
            BufferedImage imageToWrite = image;
            if (format.equals("jpeg") || format.equals("jpg")) {
                imageToWrite = removeAlphaChannel(image);
            }

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                // 如果不支持该格式，回退到 PNG
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            int q = quality != null ? quality : 90;
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(q / 100f);
            }

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(imageToWrite, null, null), param);
            } finally {
                writer.dispose();
            }
        }

        return baos.toByteArray();
    }

    /**
     * 移除图片的透明通道（Alpha通道）
     * JPEG格式不支持透明，需要转换为RGB
     */
    private BufferedImage removeAlphaChannel(BufferedImage image) {
        // 如果图片没有透明通道，直接返回
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }

        // 创建RGB类型的图片（无透明通道）
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        // 绘制图片（透明部分会变成白色背景）
        Graphics2D g2d = rgbImage.createGraphics();
        // 先填充白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        // 再绘制原图
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rgbImage;
    }

    /**
     * 转换图片为RGB颜色空间
     * 处理CMYK等特殊颜色空间的图片
     */
    private BufferedImage convertToRGB(BufferedImage sourceImage) {
        // 如果图片已经是RGB或ARGB类型，直接返回
        if (sourceImage.getType() == BufferedImage.TYPE_INT_RGB ||
            sourceImage.getType() == BufferedImage.TYPE_INT_ARGB ||
            sourceImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
            return sourceImage;
        }

        // 创建RGB类型的图片
        BufferedImage rgbImage = new BufferedImage(
                sourceImage.getWidth(),
                sourceImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        // 绘制并转换颜色空间
        Graphics2D g2d = rgbImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.drawImage(sourceImage, 0, 0, null);
        g2d.dispose();

        return rgbImage;
    }

    /**
     * 检测图片格式
     */
    private String detectFormat(String base64Data) {
        // 根据 Base64 头部特征检测格式
        if (base64Data.startsWith("/9j/")) {
            return "jpeg";
        } else if (base64Data.startsWith("iVBORw0KGgo")) {
            return "png";
        } else if (base64Data.startsWith("R0lGOD") || base64Data.startsWith("Qk0")) {
            return "gif";
        } else if (base64Data.startsWith("UklGR")) {
            return "webp";
        } else if (base64Data.startsWith("Qk0")) {
            return "bmp";
        }
        return "png"; // 默认
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return "图片格式错误: " + e.getMessage();
        }
        if (e instanceof BusinessException) {
            return e.getMessage();
        }
        return "添加水印失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "图片水印",
                "description", "为图片添加文字或图片水印，支持自定义位置、透明度、旋转角度",
                "supportedFormats", new String[]{"jpeg", "png", "webp", "bmp", "gif"},
                "outputFormats", new String[]{"original", "jpeg", "png", "webp"},
                "maxFileSize", 10 * 1024 * 1024, // 10MB
                "features", new String[]{"text", "image", "position", "opacity", "rotation", "tile"}
        );
    }

    /**
     * 请求参数
     */
    @Data
    public static class ImageWatermarkRequest {
        /**
         * 原图 Base64 数据
         */
        private String imageData;
        /**
         * 水印类型：text、image
         */
        private String watermarkType;
        /**
         * 水印文字（文字水印时必填）
         */
        private String text;
        /**
         * 字体
         */
        private String fontFamily;
        /**
         * 字号（12-200）
         */
        private Integer fontSize;
        /**
         * 字体颜色（十六进制）
         */
        private String fontColor;
        /**
         * 字体粗细：normal、bold
         */
        private String fontWeight;
        /**
         * 水印图片 Base64 数据（图片水印时必填）
         */
        private String watermarkImageData;
        /**
         * 水印图片缩放比例（10-100）
         */
        private Integer watermarkScale;
        /**
         * 水印位置
         */
        private String position;
        /**
         * 透明度（0-100）
         */
        private int opacity;
        /**
         * 旋转角度（0-360）
         */
        private int rotation;
        /**
         * 水平边距
         */
        private int marginX;
        /**
         * 垂直边距
         */
        private int marginY;
        /**
         * 输出格式
         */
        private String outputFormat;
        /**
         * 输出质量（1-100）
         */
        private Integer quality;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ImageWatermarkResponse extends BaseToolResponse {
        /**
         * 原始图片 Base64 数据
         */
        private String originalData;
        /**
         * 带水印图片 Base64 数据
         */
        private String watermarkedData;
        /**
         * 原始图片大小
         */
        private long originalSize;
        /**
         * 带水印图片大小
         */
        private long watermarkedSize;
        /**
         * 原始图片宽度
         */
        private int originalWidth;
        /**
         * 原始图片高度
         */
        private int originalHeight;
        /**
         * 输出格式
         */
        private String format;
    }
}
