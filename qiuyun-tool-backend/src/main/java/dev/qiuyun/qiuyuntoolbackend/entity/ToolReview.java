package dev.qiuyun.qiuyuntoolbackend.entity;

import dev.qiuyun.qiuyuntoolbackend.enums.ReviewStatus;
import dev.qiuyun.qiuyuntoolbackend.enums.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 工具评论实体
 */
@Entity
@Table(name = "tool_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column
    private Integer rating;  // 主评论必填，回复可为null

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "reply_count")
    @Builder.Default
    private Integer replyCount = 0;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "image_urls")
    private String imageUrls;  // JSON数组存储图片URL

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "review_type")
    @Builder.Default
    private ReviewType reviewType = ReviewType.REVIEW;

    @Enumerated(EnumType.ORDINAL)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ENABLED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否是评论（带评分的顶级评论）
     */
    public boolean isReview() {
        return reviewType != null && reviewType.isReview();
    }

    /**
     * 是否是回复
     */
    public boolean isReply() {
        return reviewType != null && reviewType.isReply();
    }

    /**
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 增加回复数
     */
    public void incrementReplyCount() {
        this.replyCount++;
    }
}
