package dev.qiuyun.qiuyuntoolbackend.executor.crypto;

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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * HMAC 生成器执行器
 * 支持：HMAC-SHA1/SHA256/SHA512/MD5 等多种算法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HmacGeneratorExecutor extends AbstractToolExecutor<HmacGeneratorExecutor.GeneratorRequest, HmacGeneratorExecutor.GeneratorResponse> {

    private static final Set<String> VALID_ALGORITHMS = Set.of("HmacSHA1", "HmacSHA256", "HmacSHA384", "HmacSHA512", "HmacMD5");
    private static final Set<String> VALID_OUTPUT_FORMATS = Set.of("hex", "base64");

    @Override
    public String getToolCode() {
        return "hmac-generator";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(GeneratorRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getMessage(), "消息内容");
        validateNotEmpty(request.getSecret(), "密钥");
        
        if (request.getAlgorithm() != null) {
            validateEnum(request.getAlgorithm(), "算法", VALID_ALGORITHMS);
        }
        
        if (request.getOutputFormat() != null) {
            validateEnum(request.getOutputFormat(), "输出格式", VALID_OUTPUT_FORMATS);
        }
    }

    @Override
    protected GeneratorResponse doExecute(GeneratorRequest request, ToolContext context) throws Exception {
        String algorithm = request.getAlgorithm() != null ? request.getAlgorithm() : "HmacSHA256";
        String outputFormat = request.getOutputFormat() != null ? request.getOutputFormat() : "hex";
        String message = request.getMessage();
        String secret = request.getSecret();

        // 生成 HMAC
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

        // 格式化输出
        String hmac;
        if ("base64".equals(outputFormat)) {
            hmac = Base64.getEncoder().encodeToString(hmacBytes);
        } else {
            hmac = bytesToHex(hmacBytes);
        }

        GeneratorResponse response = new GeneratorResponse();
        response.setSuccess(true);
        response.setAlgorithm(algorithm);
        response.setOutputFormat(outputFormat);
        response.setHmac(hmac);
        response.setMessageLength(message.length());
        response.setSecretLength(secret.length());

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "HMAC 生成失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "HMAC生成器",
                "description", "生成 HMAC 消息认证码，支持多种哈希算法",
                "algorithms", Map.of(
                        "HmacSHA1", "HMAC-SHA1",
                        "HmacSHA256", "HMAC-SHA256 (推荐)",
                        "HmacSHA384", "HMAC-SHA384",
                        "HmacSHA512", "HMAC-SHA512",
                        "HmacMD5", "HMAC-MD5 (不推荐)"
                ),
                "outputFormats", Map.of(
                        "hex", "十六进制 (小写)",
                        "base64", "Base64"
                ),
                "defaultAlgorithm", "HmacSHA256",
                "defaultOutputFormat", "hex"
        );
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class GeneratorRequest {
        /**
         * 消息内容
         */
        private String message;
        /**
         * 密钥
         */
        private String secret;
        /**
         * 算法：HmacSHA1, HmacSHA256, HmacSHA384, HmacSHA512, HmacMD5
         */
        private String algorithm;
        /**
         * 输出格式：hex, base64
         */
        private String outputFormat;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GeneratorResponse extends BaseToolResponse {
        private String algorithm;
        private String outputFormat;
        private String hmac;
        private int messageLength;
        private int secretLength;
    }
}
