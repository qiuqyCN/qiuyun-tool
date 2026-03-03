package dev.qiuyun.qiuyuntoolbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:your-256-bit-secret-key-here-at-least-32-characters}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:7200000}") // 2小时
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7天
    private long refreshTokenExpiration;

    @Value("${jwt.issuer:qiuyun-tool}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, accessTokenExpiration, "access");
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, refreshTokenExpiration, "refresh");
    }

    /**
     * 生成令牌
     */
    private String generateToken(Long userId, String username, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(issuer)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 获取令牌类型
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT token is empty or null: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 检查是否是访问令牌
     */
    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    /**
     * 检查是否是刷新令牌
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    /**
     * 获取令牌过期时间
     */
    public Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 解析令牌
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取访问令牌过期时间（毫秒）
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * 获取刷新令牌过期时间（毫秒）
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
