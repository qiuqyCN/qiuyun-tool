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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 图片压缩工具执行器
 * 支持：JPG、PNG、WebP 格式压缩，质量调节，尺寸调整
 */
@Slf4j
@Component
public class ImageCompressExecutor extends AbstractToolExecutor<ImageCompressExecutor.ImageCompressRequest, ImageCompressExecutor.ImageCompressResponse> {

    private static final Set<String> VALID_FORMATS = Set.of("jpeg", "jpg", "png", "webp");
    private static final Set<String> VALID_OUTPUT_FORMATS = Set.of("original", "jpeg", "png", "webp");

    @Override
    public String getToolCode() {
        return "image-compress";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ImageCompressRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getImageData(), "图片数据");
        validateNotEmpty(request.getFormat(), "图片格式");
        validateEnum(request.getFormat(), "图片格式", VALID_FORMATS);

        if (request.getQuality() != null) {
            validateRange(request.getQuality(), "压缩质量", 1, 100);
        }
        if (request.getMaxWidth() != null) {
            validateRange(request.getMaxWidth(), "最大宽度", 1, 10000);
        }
        if (request.getMaxHeight() != null) {
            validateRange(request.getMaxHeight(), "最大高度", 1, 10000);
        }
    }

    @Override
    protected ImageCompressResponse doExecute(ImageCompressRequest request, ToolContext context) throws Exception {
        String imageData = request.getImageData();
        String format = request.getFormat().toLowerCase();
        int quality = request.getQuality() != null ? request.getQuality() : 80;
        Integer maxWidth = request.getMaxWidth();
        Integer maxHeight = request.getMaxHeight();
        String outputFormat = request.getOutputFormat() != null ? request.getOutputFormat() : "original";

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

        // 计算新尺寸
        BufferedImage resizedImage = originalImage;
        if (maxWidth != null || maxHeight != null) {
            int newWidth = originalWidth;
            int newHeight = originalHeight;

            if (maxWidth != null && newWidth > maxWidth) {
                newHeight = (int) ((double) newHeight * maxWidth / newWidth);
                newWidth = maxWidth;
            }
            if (maxHeight != null && newHeight > maxHeight) {
                newWidth = (int) ((double) newWidth * maxHeight / newHeight);
                newHeight = maxHeight;
            }

            if (newWidth != originalWidth || newHeight != originalHeight) {
                resizedImage = resizeImage(originalImage, newWidth, newHeight);
            }
        }

        // 确定输出格式
        String targetFormat = outputFormat.equals("original") ? format : outputFormat;
        if (targetFormat.equals("jpg")) {
            targetFormat = "jpeg";
        }

        // 压缩图片
        byte[] compressedBytes = compressImage(resizedImage, targetFormat, quality);

        // 编码为 Base64
        String compressedData = Base64.getEncoder().encodeToString(compressedBytes);

        // 构建响应
        ImageCompressResponse response = new ImageCompressResponse();
        response.setSuccess(true);
        response.setOriginalData(imageData);
        response.setCompressedData(compressedData);
        response.setOriginalSize(originalSize);
        response.setCompressedSize(compressedBytes.length);
        response.setOriginalWidth(originalWidth);
        response.setOriginalHeight(originalHeight);
        response.setCompressedWidth(resizedImage.getWidth());
        response.setCompressedHeight(resizedImage.getHeight());
        response.setFormat(targetFormat);
        response.setQuality(quality);
        response.setCompressionRatio(calculateCompressionRatio(originalSize, compressedBytes.length));

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return "图片格式错误: " + e.getMessage();
        }
        return "图片压缩失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "图片压缩",
                "description", "在线图片压缩工具，支持 JPG、PNG、WebP 格式",
                "supportedFormats", new String[]{"jpeg", "png", "webp"},
                "outputFormats", new String[]{"original", "jpeg", "png", "webp"},
                "maxFileSize", 10 * 1024 * 1024, // 10MB
                "maxDimensions", Map.of("width", 10000, "height", 10000),
                "defaultQuality", 80
        );
    }

    /**
     * 调整图片尺寸
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight) {
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        // 设置高质量渲染
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * 压缩图片
     */
    private byte[] compressImage(BufferedImage image, String format, int quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (format.equals("png")) {
            // PNG 使用无损压缩，通过调整颜色深度来减小体积
            ImageIO.write(image, "png", baos);
        } else {
            // JPEG 和 WebP 使用质量压缩
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                throw new IOException("不支持的图片格式: " + format);
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality / 100f);
            }

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }
        }

        return baos.toByteArray();
    }

    /**
     * 计算压缩比例
     */
    private double calculateCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize == 0) return 0;
        return Math.round((1 - (double) compressedSize / originalSize) * 100 * 100) / 100.0;
    }

    /**
     * 请求参数
     */
    @Data
    public static class ImageCompressRequest {
        /**
         * 图片 Base64 数据（不包含 data:image 前缀）
         */
        private String imageData;
        /**
         * 图片格式：jpeg、png、webp
         */
        private String format;
        /**
         * 压缩质量（1-100），默认 80
         */
        private Integer quality;
        /**
         * 最大宽度（可选）
         */
        private Integer maxWidth;
        /**
         * 最大高度（可选）
         */
        private Integer maxHeight;
        /**
         * 输出格式：original、jpeg、png、webp，默认 original
         */
        private String outputFormat;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ImageCompressResponse extends BaseToolResponse {
        /**
         * 原始图片 Base64 数据
         */
        private String originalData;
        /**
         * 压缩后图片 Base64 数据
         */
        private String compressedData;
        /**
         * 原始图片大小（字节）
         */
        private long originalSize;
        /**
         * 压缩后图片大小（字节）
         */
        private long compressedSize;
        /**
         * 原始图片宽度
         */
        private int originalWidth;
        /**
         * 原始图片高度
         */
        private int originalHeight;
        /**
         * 压缩后图片宽度
         */
        private int compressedWidth;
        /**
         * 压缩后图片高度
         */
        private int compressedHeight;
        /**
         * 输出格式
         */
        private String format;
        /**
         * 压缩质量
         */
        private int quality;
        /**
         * 压缩比例（百分比）
         */
        private double compressionRatio;
    }
}
