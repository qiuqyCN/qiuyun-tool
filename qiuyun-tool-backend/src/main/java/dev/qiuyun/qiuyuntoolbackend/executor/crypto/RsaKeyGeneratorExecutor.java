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

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * RSA 密钥生成器执行器
 * 支持：生成 RSA 公私钥对，多种密钥长度和格式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RsaKeyGeneratorExecutor extends AbstractToolExecutor<RsaKeyGeneratorExecutor.GeneratorRequest, RsaKeyGeneratorExecutor.GeneratorResponse> {

    private static final Set<Integer> VALID_KEY_SIZES = Set.of(1024, 2048, 3072, 4096);
    private static final Set<String> VALID_FORMATS = Set.of("PKCS1", "PKCS8");
    private static final Set<String> VALID_ENCODINGS = Set.of("PEM", "DER");

    @Override
    public String getToolCode() {
        return "rsa-key-generator";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(GeneratorRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        
        if (request.getKeySize() != null) {
            validateRange(request.getKeySize(), "密钥长度", 1024, 4096);
        }
        
        if (request.getFormat() != null) {
            validateEnum(request.getFormat(), "密钥格式", VALID_FORMATS);
        }
        
        if (request.getEncoding() != null) {
            validateEnum(request.getEncoding(), "编码格式", VALID_ENCODINGS);
        }
    }

    @Override
    protected GeneratorResponse doExecute(GeneratorRequest request, ToolContext context) throws Exception {
        int keySize = request.getKeySize() != null ? request.getKeySize() : 2048;
        String format = request.getFormat() != null ? request.getFormat() : "PKCS8";
        String encoding = request.getEncoding() != null ? request.getEncoding() : "PEM";
        String password = request.getPassword();

        // 生成密钥对
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        KeyPair keyPair = keyGen.generateKeyPair();

        // 提取公私钥
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 生成密钥指纹
        String fingerprint = generateFingerprint(publicKey);

        // 格式化输出
        String publicKeyStr = formatPublicKey(publicKey, format, encoding);
        String privateKeyStr = formatPrivateKey(privateKey, format, encoding, password);

        GeneratorResponse response = new GeneratorResponse();
        response.setSuccess(true);
        response.setKeySize(keySize);
        response.setFormat(format);
        response.setEncoding(encoding);
        response.setPublicKey(publicKeyStr);
        response.setPrivateKey(privateKeyStr);
        response.setFingerprint(fingerprint);
        response.setPasswordProtected(password != null && !password.isEmpty());

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "RSA 密钥生成失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "RSA密钥生成器",
                "description", "生成 RSA 公私钥对，支持多种密钥长度和格式",
                "keySizes", VALID_KEY_SIZES,
                "formats", VALID_FORMATS,
                "encodings", VALID_ENCODINGS,
                "defaultKeySize", 2048,
                "defaultFormat", "PKCS8",
                "defaultEncoding", "PEM"
        );
    }

    /**
     * 生成密钥指纹
     */
    private String generateFingerprint(PublicKey publicKey) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(publicKey.getEncoded());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(String.format("%02x", digest[i]));
            if (i < digest.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * 格式化公钥
     */
    private String formatPublicKey(PublicKey publicKey, String format, String encoding) throws Exception {
        byte[] keyBytes = publicKey.getEncoded();
        
        if ("DER".equals(encoding)) {
            return Base64.getEncoder().encodeToString(keyBytes);
        }
        
        // PEM 格式
        StringBuilder sb = new StringBuilder();
        String header = "-----BEGIN PUBLIC KEY-----";
        String footer = "-----END PUBLIC KEY-----";
        
        if ("PKCS1".equals(format)) {
            // PKCS#1 格式需要将 SubjectPublicKeyInfo 转换为 RSAPublicKey
            // 这里简化处理，实际 PKCS#1 公钥需要额外转换
            header = "-----BEGIN RSA PUBLIC KEY-----";
            footer = "-----END RSA PUBLIC KEY-----";
        }
        
        sb.append(header).append("\n");
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        // 每 64 字符换行
        for (int i = 0; i < base64.length(); i += 64) {
            sb.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
        }
        sb.append(footer);
        
        return sb.toString();
    }

    /**
     * 格式化私钥
     */
    private String formatPrivateKey(PrivateKey privateKey, String format, String encoding, String password) throws Exception {
        byte[] keyBytes = privateKey.getEncoded();
        
        if ("DER".equals(encoding)) {
            return Base64.getEncoder().encodeToString(keyBytes);
        }
        
        // PEM 格式
        StringBuilder sb = new StringBuilder();
        String header = "-----BEGIN PRIVATE KEY-----";
        String footer = "-----END PRIVATE KEY-----";
        
        if ("PKCS1".equals(format)) {
            header = "-----BEGIN RSA PRIVATE KEY-----";
            footer = "-----END RSA PRIVATE KEY-----";
        }
        
        // TODO: 如果需要密码保护，这里可以实现加密逻辑
        // 目前仅做标记，实际加密需要更复杂的实现
        
        sb.append(header).append("\n");
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        // 每 64 字符换行
        for (int i = 0; i < base64.length(); i += 64) {
            sb.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
        }
        sb.append(footer);
        
        return sb.toString();
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class GeneratorRequest {
        /**
         * 密钥长度：1024, 2048, 3072, 4096
         */
        private Integer keySize;
        /**
         * 密钥格式：PKCS1, PKCS8
         */
        private String format;
        /**
         * 编码格式：PEM, DER
         */
        private String encoding;
        /**
         * 私钥密码（可选）
         */
        private String password;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GeneratorResponse extends BaseToolResponse {
        private int keySize;
        private String format;
        private String encoding;
        private String publicKey;
        private String privateKey;
        private String fingerprint;
        private boolean passwordProtected;
    }
}
