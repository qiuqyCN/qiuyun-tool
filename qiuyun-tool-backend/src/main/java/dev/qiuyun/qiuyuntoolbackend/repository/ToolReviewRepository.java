package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolReview;
import dev.qiuyun.qiuyuntoolbackend.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评论Repository
 */
@Repository
public interface ToolReviewRepository extends JpaRepository<ToolReview, Long> {

    /**
     * 分页查询工具的评论（按时间排序）
     */
    @Query("SELECT r FROM ToolReview r WHERE r.toolId = :toolId AND r.reviewType = dev.qiuyun.qiuyuntoolbackend.enums.ReviewType.REVIEW AND r.status = 1 ORDER BY r.createdAt DESC")
    Page<ToolReview> findReviewsByNewest(@Param("toolId") Long toolId, Pageable pageable);

    /**
     * 分页查询工具的评论（按热度排序）
     */
    @Query("SELECT r FROM ToolReview r WHERE r.toolId = :toolId AND r.reviewType = dev.qiuyun.qiuyuntoolbackend.enums.ReviewType.REVIEW AND r.status = 1 ORDER BY r.likeCount DESC, r.createdAt DESC")
    Page<ToolReview> findReviewsByHot(@Param("toolId") Long toolId, Pageable pageable);

    /**
     * 查询评论的回复列表
     */
    @Query("SELECT r FROM ToolReview r WHERE r.parentId = :parentId AND r.status = 1 ORDER BY r.createdAt ASC")
    List<ToolReview> findReplies(@Param("parentId") Long parentId);

    /**
     * 查询评论的前N条回复
     */
    @Query("SELECT r FROM ToolReview r WHERE r.parentId = :parentId AND r.status = 1 ORDER BY r.createdAt ASC")
    List<ToolReview> findTopReplies(@Param("parentId") Long parentId, Pageable pageable);

    /**
     * 查询用户是否已评论过该工具（只检查有效评论）
     */
    @Query("SELECT COUNT(r) > 0 FROM ToolReview r WHERE r.toolId = :toolId AND r.userId = :userId AND r.reviewType = :reviewType AND r.status = 1")
    boolean existsByToolIdAndUserIdAndReviewType(@Param("toolId") Long toolId, @Param("userId") Long userId, @Param("reviewType") ReviewType reviewType);

    /**
     * 查询用户对工具的评论（只查询有效评论）
     */
    @Query("SELECT r FROM ToolReview r WHERE r.toolId = :toolId AND r.userId = :userId AND r.reviewType = :reviewType AND r.status = 1")
    Optional<ToolReview> findByToolIdAndUserIdAndReviewType(@Param("toolId") Long toolId, @Param("userId") Long userId, @Param("reviewType") ReviewType reviewType);

    /**
     * 统计工具的有效评论数
     */
    long countByToolIdAndStatus(Long toolId, Integer status);

    /**
     * 计算工具的平均评分（只统计评论）
     */
    @Query("SELECT AVG(r.rating) FROM ToolReview r WHERE r.toolId = :toolId AND r.reviewType = dev.qiuyun.qiuyuntoolbackend.enums.ReviewType.REVIEW AND r.rating IS NOT NULL AND r.status = 1")
    Double calculateAverageRating(@Param("toolId") Long toolId);

    /**
     * 统计各星级的评论数（只统计评论）
     */
    @Query("SELECT r.rating, COUNT(r) FROM ToolReview r WHERE r.toolId = :toolId AND r.reviewType = dev.qiuyun.qiuyuntoolbackend.enums.ReviewType.REVIEW AND r.rating IS NOT NULL AND r.status = 1 GROUP BY r.rating")
    List<Object[]> countByRating(@Param("toolId") Long toolId);

    /**
     * 统计有评分的评论数（用于计算占比）
     */
    @Query("SELECT COUNT(r) FROM ToolReview r WHERE r.toolId = :toolId AND r.reviewType = dev.qiuyun.qiuyuntoolbackend.enums.ReviewType.REVIEW AND r.rating IS NOT NULL AND r.status = 1")
    long countRatedReviews(@Param("toolId") Long toolId);
}
