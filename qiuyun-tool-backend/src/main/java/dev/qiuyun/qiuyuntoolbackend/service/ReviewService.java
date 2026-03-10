package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.response.review.ReviewResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.review.ReviewStatsResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.request.review.SubmitReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 评论服务接口
 */
public interface ReviewService {

    /**
     * 提交评论
     */
    ReviewResponse submitReview(SubmitReviewRequest request, Long userId);

    /**
     * 修改评论
     */
    ReviewResponse updateReview(Long reviewId, SubmitReviewRequest request, Long userId);

    /**
     * 获取工具评论列表
     */
    Page<ReviewResponse> getToolReviews(Long toolId, String sort, Pageable pageable, Long currentUserId);

    /**
     * 获取评论回复列表
     */
    List<ReviewResponse> getReplies(Long reviewId, Long currentUserId);

    /**
     * 点赞/取消点赞
     */
    void toggleLike(Long reviewId, Long userId);

    /**
     * 删除评论
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 获取评论统计
     */
    ReviewStatsResponse getReviewStats(Long toolId);

    /**
     * 检查用户是否已评论
     */
    boolean hasReviewed(Long toolId, Long userId);
}
