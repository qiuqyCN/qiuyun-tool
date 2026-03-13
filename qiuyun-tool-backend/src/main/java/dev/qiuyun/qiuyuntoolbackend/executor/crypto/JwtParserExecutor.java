package dev.qiuyun.qiuyuntoolbackend.executor.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JWT 解析器执行器
 * 支持：解析 JWT Token、验证签名、查看过期时间
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtParserExecutor extends AbstractToolExecutor<JwtParserExecutor.ParserRequest, JwtParserExecutor.ParserResponse> {

    private final ObjectMapper objectMapper;

    @Override
    public String getToolCode() {
        return "jwt-parser";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(ParserRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getToken(), "JWT Token");

        // 基本格式验证
        String token = request.getToken().trim();
        if (!token.contains(".")) {
            throw new BusinessException("无效的 JWT Token 格式，应包含至少一个点号");
        }
    }

    @Override
    protected ParserResponse doExecute(ParserRequest request, ToolContext context) throws Exception {
        String token = request.getToken().trim();

        // 移除 Bearer 前缀（如果有）
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        // 分割 JWT
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException("JWT Token 应包含 3 个部分（Header.Payload.Signature），当前有 " + parts.length + " 个部分");
        }

        ParserResponse response = new ParserResponse();
        response.setSuccess(true);
        response.setRawToken(token);

        // 解析 Header
        JwtHeader header = parseHeader(parts[0]);
        response.setHeader(header);

        // 解析 Payload
        JwtPayload payload = parsePayload(parts[1]);
        response.setPayload(payload);

        // 解析 Signature
        String signature = parts[2];
        response.setSignature(signature);
        response.setSignatureValid(!signature.isEmpty());

        // 验证过期时间
        if (payload.getExp() != null) {
            long now = Instant.now().getEpochSecond();
            boolean isExpired = payload.getExp() < now;
            response.setExpired(isExpired);
            response.setExpiresIn(isExpired ? 0 : payload.getExp() - now);
        }

        return response;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "JWT 解析失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "JWT解析器",
                "description", "解析 JWT Token，查看 Header、Payload、过期时间等信息",
                "features", List.of(
                        "解析 JWT 结构",
                        "格式化显示 Header 和 Payload",
                        "自动检测过期时间",
                        "支持 Bearer Token 格式",
                        "显示时间戳为可读日期"
                ),
                "supportedAlgorithms", List.of("HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "none")
        );
    }

    /**
     * 解析 Header
     */
    private JwtHeader parseHeader(String headerBase64) throws Exception {
        String headerJson = base64UrlDecode(headerBase64);
        JsonNode headerNode = objectMapper.readTree(headerJson);

        JwtHeader header = new JwtHeader();
        header.setRawJson(headerJson);

        if (headerNode.has("alg")) {
            header.setAlg(headerNode.get("alg").asText());
        }
        if (headerNode.has("typ")) {
            header.setTyp(headerNode.get("typ").asText());
        }
        if (headerNode.has("kid")) {
            header.setKid(headerNode.get("kid").asText());
        }

        return header;
    }

    /**
     * 解析 Payload
     */
    private JwtPayload parsePayload(String payloadBase64) throws Exception {
        String payloadJson = base64UrlDecode(payloadBase64);
        JsonNode payloadNode = objectMapper.readTree(payloadJson);

        JwtPayload payload = new JwtPayload();
        payload.setRawJson(payloadJson);

        // 标准声明
        if (payloadNode.has("iss")) {
            payload.setIss(payloadNode.get("iss").asText());
        }
        if (payloadNode.has("sub")) {
            payload.setSub(payloadNode.get("sub").asText());
        }
        if (payloadNode.has("aud")) {
            JsonNode audNode = payloadNode.get("aud");
            if (audNode.isArray()) {
                List<String> audList = new ArrayList<>();
                audNode.forEach(node -> audList.add(node.asText()));
                payload.setAud(audList);
            } else {
                payload.setAud(List.of(audNode.asText()));
            }
        }
        if (payloadNode.has("exp")) {
            payload.setExp(payloadNode.get("exp").asLong());
            payload.setExpFormatted(formatTimestamp(payload.getExp()));
        }
        if (payloadNode.has("nbf")) {
            payload.setNbf(payloadNode.get("nbf").asLong());
            payload.setNbfFormatted(formatTimestamp(payload.getNbf()));
        }
        if (payloadNode.has("iat")) {
            payload.setIat(payloadNode.get("iat").asLong());
            payload.setIatFormatted(formatTimestamp(payload.getIat()));
        }
        if (payloadNode.has("jti")) {
            payload.setJti(payloadNode.get("jti").asText());
        }

        // 其他自定义声明
        Map<String, JsonNode> claims = new LinkedHashMap<>();
        payloadNode.properties().forEach(entry -> {
            String key = entry.getKey();
            if (!List.of("iss", "sub", "aud", "exp", "nbf", "iat", "jti").contains(key)) {
                claims.put(key, entry.getValue());
            }
        });
        payload.setCustomClaims(claims);

        return payload;
    }

    /**
     * Base64 URL 解码
     */
    private String base64UrlDecode(String base64Url) {
        // 替换 Base64 URL 安全字符
        String base64 = base64Url.replace('-', '+').replace('_', '/');

        // 补齐填充
        int padding = 4 - (base64.length() % 4);
        if (padding != 4) {
            base64 += "=".repeat(padding);
        }

        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    /**
     * 格式化时间戳
     */
    private String formatTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class ParserRequest {
        private String token;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ParserResponse extends BaseToolResponse {
        private String rawToken;
        private JwtHeader header;
        private JwtPayload payload;
        private String signature;
        private boolean signatureValid;
        private Boolean expired;
        private Long expiresIn;
    }

    @Data
    public static class JwtHeader {
        private String rawJson;
        private String alg;
        private String typ;
        private String kid;
    }

    @Data
    public static class JwtPayload {
        private String rawJson;
        private String iss;
        private String sub;
        private List<String> aud;
        private Long exp;
        private String expFormatted;
        private Long nbf;
        private String nbfFormatted;
        private Long iat;
        private String iatFormatted;
        private String jti;
        private Map<String, JsonNode> customClaims;
    }
}
