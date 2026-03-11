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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * 图片格式转换工具执行器
 * 支持：JPG、PNG、WebP、BMP、GIF 格式互转，支持透明通道处理
 */
@Slf4j
@Component
public class ImageConvertExecutor extends AbstractToolExecutor<ImageConvertExecutor.ImageConvertRequest, ImageConvertExecutor.ImageConvertResponse> {

    private static final Set<String> VALID_FORMATS = Set.of("jpeg", "jpg", "png", "webp", "bmp", "gif");
    private static final Set<String> SUPPORT_TRANSPARENCY = Set.of("png", "webp", "gif");

    @Override
    public String getToolCode() {
        return "image-convert";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ImageConvertRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getImageData(), "图片数据");
        validateNotEmpty(request.getSourceFormat(), "源格式");
        validateNotEmpty(request.getTargetFormat(), "目标格式");
        validateEnum(request.getSourceFormat(), "源格式", VALID_FORMATS);
        validateEnum(request.getTargetFormat(), "目标格式", VALID_FORMATS);

        if (request.getQuality() != null) {
            validateRange(request.getQuality(), "质量", 1, 100);
        }
    }

    @Override
    protected ImageConvertResponse doExecute(ImageConvertRequest request, ToolContext context) throws Exception {
        String imageData = request.getImageData();
        String sourceFormat = request.getSourceFormat().toLowerCase();
        String targetFormat = request.getTargetFormat().toLowerCase();
        Integer quality = request.getQuality();
        boolean keepTransparency = request.isKeepTransparency();

        // 解码 Base64 图片
        byte[] imageBytes = Base64.getDecoder().decode(imageData);

        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new BusinessException("无法读取图片数据");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        long originalSize = imageBytes.length;

        // 处理透明通道
        BufferedImage convertedImage = handleTransparency(originalImage, targetFormat, keepTransparency);

        // 转换格式
        byte[] convertedBytes = convertImage(convertedImage, targetFormat, quality);

        // 编码为 Base64
        String convertedData = Base64.getEncoder().encodeToString(convertedBytes);

        // 构建响应
        ImageConvertResponse response = new ImageConvertResponse();
        response.setSuccess(true);
        response.setOriginalData(imageData);
        response.setConvertedData(convertedData);
        response.setOriginalSize(originalSize);
        response.setConvertedSize(convertedBytes.length);
        response.setOriginalWidth(originalWidth);
        response.setOriginalHeight(originalHeight);
        response.setSourceFormat(sourceFormat);
        response.setTargetFormat(targetFormat);
        response.setHasTransparency(originalImage.getColorModel().hasAlpha() && SUPPORT_TRANSPARENCY.contains(targetFormat));

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return "图片格式错误: " + e.getMessage();
        }
        return "图片转换失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "图片格式转换",
                "description", "支持 JPG、PNG、WebP、BMP、GIF 格式互转",
                "supportedFormats", new String[]{"jpeg", "png", "webp", "bmp", "gif"},
                "transparencyFormats", SUPPORT_TRANSPARENCY,
                "maxFileSize", 10 * 1024 * 1024, // 10MB
                "features", new String[]{"transparency", "quality"}
        );
    }

    /**
     * 处理透明通道
     */
    private BufferedImage handleTransparency(BufferedImage originalImage, String targetFormat, boolean keepTransparency) {
        boolean hasAlpha = originalImage.getColorModel().hasAlpha();
        boolean targetSupportsAlpha = SUPPORT_TRANSPARENCY.contains(targetFormat);

        // 如果目标格式不支持透明通道，或者不需要保留透明通道
        if (hasAlpha && (!targetSupportsAlpha || !keepTransparency)) {
            // 创建不透明背景的图片
            BufferedImage newImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g = newImage.createGraphics();

            // 设置白色背景
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, originalImage.getWidth(), originalImage.getHeight());

            // 绘制原图
            g.drawImage(originalImage, 0, 0, null);
            g.dispose();

            return newImage;
        }

        // 如果需要保留透明通道但当前图片没有 alpha，或者格式匹配
        if (targetSupportsAlpha && keepTransparency) {
            // 确保使用支持 alpha 的类型
            if (originalImage.getType() != BufferedImage.TYPE_INT_ARGB &&
                originalImage.getType() != BufferedImage.TYPE_INT_ARGB_PRE) {
                BufferedImage newImage = new BufferedImage(
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g = newImage.createGraphics();
                g.drawImage(originalImage, 0, 0, null);
                g.dispose();
                return newImage;
            }
        }

        return originalImage;
    }

    /**
     * 转换图片格式
     */
    private byte[] convertImage(BufferedImage image, String format, Integer quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String writeFormat = format.equals("jpg") ? "jpeg" : format;

        // 对于 JPEG 格式，处理质量参数
        if (writeFormat.equals("jpeg") && quality != null) {
            javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality / 100f);

            try (javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }
        } else {
            // 其他格式直接写入
            boolean success = ImageIO.write(image, writeFormat, baos);
            if (!success) {
                throw new IOException("不支持的图片格式: " + format);
            }
        }

        return baos.toByteArray();
    }

    /**
     * 请求参数
     */
    @Data
    public static class ImageConvertRequest {
        /**
         * 图片 Base64 数据（不包含 data:image 前缀）
         */
        private String imageData;
        /**
         * 源格式：jpeg、png、webp、bmp、gif
         */
        private String sourceFormat;
        /**
         * 目标格式：jpeg、png、webp、bmp、gif
         */
        private String targetFormat;
        /**
         * 质量（1-100），仅对 JPEG 有效，默认 90
         */
        private Integer quality;
        /**
         * 是否保留透明通道，默认 true
         */
        private boolean keepTransparency = true;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ImageConvertResponse extends BaseToolResponse {
        /**
         * 原始图片 Base64 数据
         */
        private String originalData;
        /**
         * 转换后图片 Base64 数据
         */
        private String convertedData;
        /**
         * 原始图片大小（字节）
         */
        private long originalSize;
        /**
         * 转换后图片大小（字节）
         */
        private long convertedSize;
        /**
         * 原始图片宽度
         */
        private int originalWidth;
        /**
         * 原始图片高度
         */
        private int originalHeight;
        /**
         * 源格式
         */
        private String sourceFormat;
        /**
         * 目标格式
         */
        private String targetFormat;
        /**
         * 是否保留透明通道
         */
        private boolean hasTransparency;
    }
}
