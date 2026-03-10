package dev.qiuyun.qiuyuntoolbackend.payload.response;

import dev.qiuyun.qiuyuntoolbackend.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String phone;
    private Boolean isVip;
    private LocalDateTime vipExpireDate;
    private UserStatus status;
    private List<String> roles;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
}
