package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 评论点赞Repository
 */
@Repository
public interface ToolReviewLikeRepository extends JpaRepository<ToolReviewLike, Long> {

    /**
     * 查询用户是否点赞过评论
     */
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    /**
     * 查询点赞记录
     */
    Optional<ToolReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);

    /**
     * 删除点赞记录
     */
    void deleteByReviewIdAndUserId(Long reviewId, Long userId);

    /**
     * 统计评论的点赞数
     */
    long countByReviewId(Long reviewId);
}
