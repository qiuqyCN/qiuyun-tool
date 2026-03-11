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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * 图片转 Base64 工具执行器
 * 支持：任意图片格式转换为 Base64 编码字符串
 */
@Slf4j
@Component
public class ImageToBase64Executor extends AbstractToolExecutor<ImageToBase64Executor.ImageToBase64Request, ImageToBase64Executor.ImageToBase64Response> {

    // 最大文件大小 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public String getToolCode() {
        return "image-to-base64";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ImageToBase64Request request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getImageData(), "图片数据");

        // 检查文件大小（Base64 编码后的大小）
        long dataSize = request.getImageData().length() * 3L / 4; // 估算原始大小
        if (dataSize > MAX_FILE_SIZE) {
            throw new BusinessException("图片大小超过 10MB 限制");
        }
    }

    @Override
    protected ImageToBase64Response doExecute(ImageToBase64Request request, ToolContext context) throws Exception {
        String imageData = request.getImageData();
        boolean includePrefix = request.isIncludePrefix();

        // 解码 Base64 图片数据
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(imageData);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的图片数据格式");
        }

        // 验证是否为有效的图片
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (bufferedImage == null) {
            throw new BusinessException("无法读取图片数据，请确保上传的是有效的图片文件");
        }

        // 获取图片信息
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        String format = detectImageFormat(imageBytes);

        // 构建 Base64 结果
        String base64Result;
        if (includePrefix) {
            base64Result = String.format("data:image/%s;base64,%s", format, imageData);
        } else {
            base64Result = imageData;
        }

        // 构建响应
        ImageToBase64Response response = new ImageToBase64Response();
        response.setSuccess(true);
        response.setBase64Data(base64Result);
        response.setOriginalSize(imageBytes.length);
        response.setBase64Length(base64Result.length());
        response.setWidth(width);
        response.setHeight(height);
        response.setFormat(format);
        response.setHasPrefix(includePrefix);

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return "图片数据格式错误: " + e.getMessage();
        }
        return "图片转换失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "图片转 Base64",
                "description", "将图片转换为 Base64 编码字符串",
                "maxFileSize", MAX_FILE_SIZE,
                "supportedFormats", new String[]{"jpeg", "jpg", "png", "gif", "bmp", "webp"},
                "features", new String[]{"prefix", "info"}
        );
    }

    /**
     * 检测图片格式
     */
    private String detectImageFormat(byte[] imageBytes) {
        // 通过文件头（Magic Number）检测图片格式
        if (imageBytes.length < 8) {
            return "unknown";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((imageBytes[0] & 0xFF) == 0x89 && (imageBytes[1] & 0xFF) == 0x50) {
            return "png";
        }

        // JPEG: FF D8 FF
        if ((imageBytes[0] & 0xFF) == 0xFF && (imageBytes[1] & 0xFF) == 0xD8) {
            return "jpeg";
        }

        // GIF: GIF87a 或 GIF89a
        if (imageBytes[0] == 'G' && imageBytes[1] == 'I' && imageBytes[2] == 'F') {
            return "gif";
        }

        // BMP: BM
        if (imageBytes[0] == 'B' && imageBytes[1] == 'M') {
            return "bmp";
        }

        // WebP: RIFF....WEBP
        if (imageBytes[0] == 'R' && imageBytes[1] == 'I' && imageBytes[2] == 'F' && imageBytes[3] == 'F' &&
            imageBytes[8] == 'W' && imageBytes[9] == 'E' && imageBytes[10] == 'B' && imageBytes[11] == 'P') {
            return "webp";
        }

        return "unknown";
    }

    /**
     * 请求参数
     */
    @Data
    public static class ImageToBase64Request {
        /**
         * 图片 Base64 数据（不包含 data:image 前缀）
         */
        private String imageData;
        /**
         * 是否包含 data:image 前缀，默认 true
         */
        private boolean includePrefix = true;
    }

    /**
     * 响应结果
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ImageToBase64Response extends BaseToolResponse {
        /**
         * Base64 编码结果
         */
        private String base64Data;
        /**
         * 原始图片大小（字节）
         */
        private long originalSize;
        /**
         * Base64 字符串长度
         */
        private int base64Length;
        /**
         * 图片宽度
         */
        private int width;
        /**
         * 图片高度
         */
        private int height;
        /**
         * 图片格式
         */
        private String format;
        /**
         * 是否包含前缀
         */
        private boolean hasPrefix;
    }
}
