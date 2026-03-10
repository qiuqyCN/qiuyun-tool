package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.response.review.ReviewResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.review.ReviewStatsResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.request.review.SubmitReviewRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.security.CurrentUser;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 提交评论
     */
    @PostMapping
    public ApiResponse<ReviewResponse> submitReview(
            @RequestBody @Valid SubmitReviewRequest request,
            @CurrentUser UserDetailsImpl user) {
        return ApiResponse.success(reviewService.submitReview(request, user.getId()));
    }

    /**
     * 修改评论
     */
    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid SubmitReviewRequest request,
            @CurrentUser UserDetailsImpl user) {
        return ApiResponse.success(reviewService.updateReview(reviewId, request, user.getId()));
    }

    /**
     * 获取工具评论列表
     */
    @GetMapping("/tool/{toolId}")
    public ApiResponse<Page<ReviewResponse>> getToolReviews(
            @PathVariable Long toolId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @CurrentUser(required = false) UserDetailsImpl user) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.success(reviewService.getToolReviews(toolId, sort, pageable, userId));
    }

    /**
     * 获取评论回复列表
     */
    @GetMapping("/{reviewId}/replies")
    public ApiResponse<List<ReviewResponse>> getReplies(
            @PathVariable Long reviewId,
            @CurrentUser(required = false) UserDetailsImpl user) {
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.success(reviewService.getReplies(reviewId, userId));
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{reviewId}/like")
    public ApiResponse<Void> toggleLike(
            @PathVariable Long reviewId,
            @CurrentUser UserDetailsImpl user) {
        reviewService.toggleLike(reviewId, user.getId());
        return ApiResponse.success();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long reviewId,
            @CurrentUser UserDetailsImpl user) {
        reviewService.deleteReview(reviewId, user.getId());
        return ApiResponse.success();
    }

    /**
     * 获取评论统计
     */
    @GetMapping("/tool/{toolId}/stats")
    public ApiResponse<ReviewStatsResponse> getReviewStats(@PathVariable Long toolId) {
        return ApiResponse.success(reviewService.getReviewStats(toolId));
    }

    /**
     * 检查用户是否已评论
     */
    @GetMapping("/tool/{toolId}/has-reviewed")
    public ApiResponse<Boolean> hasReviewed(
            @PathVariable Long toolId,
            @CurrentUser UserDetailsImpl user) {
        return ApiResponse.success(reviewService.hasReviewed(toolId, user.getId()));
    }
}
