package dev.qiuyun.qiuyuntoolbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 评论点赞实体
 */
@Entity
@Table(name = "tool_review_likes", indexes = {
        // 复合索引：查询评论的点赞数
        // 覆盖场景：countByReviewId - WHERE review_id=?
        @Index(name = "idx_like_review", columnList = "review_id"),
        // 复合索引：查询用户点赞的评论（用于批量查询）
        // 覆盖场景：findLikedReviewIdsByUserId - WHERE review_id IN (?) AND user_id=?
        @Index(name = "idx_like_user_review", columnList = "user_id, review_id"),
        // 唯一索引：防止重复点赞
        @Index(name = "idx_like_unique", columnList = "review_id, user_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
