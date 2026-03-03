package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应
 */
@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    /**
     * 用户信息
     */
    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
        private Boolean isVip;
        private List<String> roles;
    }
}
