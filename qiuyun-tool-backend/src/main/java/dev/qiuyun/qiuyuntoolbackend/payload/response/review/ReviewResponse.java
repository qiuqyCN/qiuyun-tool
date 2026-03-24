package dev.qiuyun.qiuyuntoolbackend.payload.response.review;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolReview;
import dev.qiuyun.qiuyuntoolbackend.enums.ReviewType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应
 */
@Data
public class ReviewResponse {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * 从 ToolReview 实体创建响应对象
     * 注意：用户信息、点赞状态等需要额外设置
     */
    public static ReviewResponse from(ToolReview review) {
        if (review == null) {
            return null;
        }
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setToolId(review.getToolId());
        response.setUserId(review.getUserId());
        response.setRating(review.getRating());
        response.setContent(review.getContent());
        response.setLikeCount(review.getLikeCount());
        response.setReplyCount(review.getReplyCount());
        response.setReviewType(review.getReviewType());
        response.setCreatedAt(review.getCreatedAt());

        // 解析图片URL
        if (review.getImageUrls() != null) {
            try {
                response.setImageUrls(objectMapper.readValue(review.getImageUrls(), new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                // 解析失败时忽略
            }
        }

        return response;
    }
}
