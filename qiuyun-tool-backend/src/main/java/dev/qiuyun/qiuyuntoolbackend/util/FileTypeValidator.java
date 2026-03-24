package dev.qiuyun.qiuyuntoolbackend.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型验证器
 * 使用文件头魔数（Magic Number）验证文件真实类型
 */
public class FileTypeValidator {

    // 支持的图片文件魔数
    private static final Map<String, byte[]> IMAGE_MAGIC_NUMBERS = new HashMap<>();

    static {
        // JPEG/JPG: FF D8 FF
        IMAGE_MAGIC_NUMBERS.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        // PNG: 89 50 4E 47
        IMAGE_MAGIC_NUMBERS.put("image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        // GIF: 47 49 46 38
        IMAGE_MAGIC_NUMBERS.put("image/gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        // WebP: 52 49 46 46
        IMAGE_MAGIC_NUMBERS.put("image/webp", new byte[]{0x52, 0x49, 0x46, 0x46});
        // BMP: 42 4D
        IMAGE_MAGIC_NUMBERS.put("image/bmp", new byte[]{0x42, 0x4D});
    }

    /**
     * 验证文件是否为有效的图片
     *
     * @param file 上传的文件
     * @return 验证结果
     */
    public static boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // 获取对应的魔数
        byte[] expectedMagic = IMAGE_MAGIC_NUMBERS.get(contentType);
        if (expectedMagic == null) {
            // 不支持的图片类型
            return false;
        }

        try (InputStream is = file.getInputStream()) {
            byte[] fileHeader = new byte[expectedMagic.length];
            int read = is.read(fileHeader);

            if (read < expectedMagic.length) {
                return false;
            }

            return Arrays.equals(fileHeader, expectedMagic);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取文件的真实类型（基于魔数）
     *
     * @param file 上传的文件
     * @return 真实的MIME类型，如果无法识别则返回null
     */
    public static String getRealContentType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try (InputStream is = file.getInputStream()) {
            byte[] fileHeader = new byte[8];
            int read = is.read(fileHeader);

            if (read < 2) {
                return null;
            }

            // 检查各种图片类型
            for (Map.Entry<String, byte[]> entry : IMAGE_MAGIC_NUMBERS.entrySet()) {
                byte[] magic = entry.getValue();
                if (read >= magic.length) {
                    byte[] fileMagic = Arrays.copyOf(fileHeader, magic.length);
                    if (Arrays.equals(fileMagic, magic)) {
                        return entry.getKey();
                    }
                }
            }

            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 检查文件类型是否与声明的一致
     *
     * @param file        上传的文件
     * @param contentType 声明的MIME类型
     * @return 是否一致
     */
    public static boolean isContentTypeMatch(MultipartFile file, String contentType) {
        String realType = getRealContentType(file);
        return realType != null && realType.equals(contentType);
    }
}
