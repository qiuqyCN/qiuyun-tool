package dev.qiuyun.qiuyuntoolbackend.util.converter;

import dev.qiuyun.qiuyuntoolbackend.constant.RoleConstants;
import dev.qiuyun.qiuyuntoolbackend.entity.User;
import dev.qiuyun.qiuyuntoolbackend.payload.response.review.ReviewResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 用户信息转换器
 * 统一处理用户信息设置到响应对象
 */
@Component
public class UserConverter {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 设置用户信息到评论响应
     */
    public void setUserInfo(ReviewResponse response, User user) {
        if (user == null || response == null) {
            return;
        }
        response.setUserNickname(user.getNickname());
        response.setUserAvatar(resolveAvatarUrl(user.getAvatar()));
        response.setIsVip(user.getIsVip() != null && user.getIsVip());
        response.setIsAdmin(user.getRoles().stream()
                .anyMatch(role -> RoleConstants.ROLE_ADMIN.equals(role.getRole())));
    }

    /**
     * 解析头像URL，如果是相对路径则添加baseUrl
     */
    public String resolveAvatarUrl(String avatar) {
        if (avatar == null || avatar.isEmpty()) {
            return baseUrl + "/default-avatar.png";
        }
        // 如果已经是完整URL，直接返回
        if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
            return avatar;
        }
        // 相对路径，添加baseUrl
        return baseUrl + avatar;
    }
}
