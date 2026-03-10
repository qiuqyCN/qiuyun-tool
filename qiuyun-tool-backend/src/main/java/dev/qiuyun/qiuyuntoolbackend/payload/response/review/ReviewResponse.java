package dev.qiuyun.qiuyuntoolbackend.payload.response.review;

import dev.qiuyun.qiuyuntoolbackend.enums.ReviewType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应
 */
@Data
public class ReviewResponse {

    private Long id;
    private Long toolId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Integer rating;
    private String content;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isLiked;
    private Boolean isOwner;
    private Boolean isAdmin;
    private Boolean isVip;
    private ReviewType reviewType;
    private List<ReviewResponse> replies;
    private LocalDateTime createdAt;

    /**
     * 获取脱敏后的用户名
     */
    public String getDisplayName() {
        return userNickname;
    }

    /**
     * 获取默认头像
     */
    public String getDisplayAvatar() {
        if (userAvatar == null) {
            return "/default-avatar.png";
        }
        return userAvatar;
    }
}
