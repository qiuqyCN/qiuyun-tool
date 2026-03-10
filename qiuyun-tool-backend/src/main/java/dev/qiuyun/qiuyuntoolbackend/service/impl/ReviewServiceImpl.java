package dev.qiuyun.qiuyuntoolbackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.qiuyun.qiuyuntoolbackend.payload.response.review.ReviewResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.review.ReviewStatsResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.request.review.SubmitReviewRequest;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolReview;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolReviewLike;
import dev.qiuyun.qiuyuntoolbackend.entity.User;
import dev.qiuyun.qiuyuntoolbackend.enums.ReviewStatus;
import dev.qiuyun.qiuyuntoolbackend.enums.ReviewType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolReviewLikeRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolReviewRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.UserRepository;
import dev.qiuyun.qiuyuntoolbackend.service.ReviewService;
import dev.qiuyun.qiuyuntoolbackend.service.TempImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ToolReviewRepository reviewRepository;
    private final ToolReviewLikeRepository likeRepository;
    private final UserRepository userRepository;
    private final TempImageService tempImageService;
    private final ObjectMapper objectMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional
    public ReviewResponse submitReview(SubmitReviewRequest request, Long userId) {
        // 检查是否是回复别人的评论
        if (request.getParentId() != null) {
            return submitReply(request, userId);
        }

        // 评论必须评分
        if (request.getRating() == null) {
            throw new BusinessException("评分不能为空");
        }

        ToolReview review = ToolReview.builder()
                .toolId(request.getToolId())
                .userId(userId)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrls(convertImageUrls(request.getImageUrls()))
                .reviewType(ReviewType.REVIEW)
                .status(ReviewStatus.ENABLED)
                .build();

        reviewRepository.save(review);

        // 标记图片为已关联
        tempImageService.markImagesAsLinked(request.getImageUrls());

        ReviewResponse response = convertToResponse(review, userId);

        // 查询并设置用户信息
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            response.setUserNickname(user.getNickname());
            response.setUserAvatar(resolveAvatarUrl(user.getAvatar()));
        }

        return response;
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, SubmitReviewRequest request, Long userId) {
        ToolReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        // 只能修改自己的评论
        if (!Objects.equals(review.getUserId(), userId)) {
            throw new BusinessException("只能修改自己的评论");
        }

        // 只能修改评论（REVIEW），不能修改回复（REPLY）
        if (!review.isReview()) {
            throw new BusinessException("只能修改评论");
        }

        // 更新评分和内容
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        
        // 获取更新前的图片列表
        List<String> oldImageUrls = parseImageUrls(review.getImageUrls());
        // 获取新的图片列表（为空则表示全部删除）
        List<String> newImageUrls = request.getImageUrls() != null ? request.getImageUrls() : List.of();
        
        // 更新图片
        review.setImageUrls(convertImageUrls(newImageUrls));
        reviewRepository.save(review);

        // 标记新图片为已关联（可能已被定时任务删除，但不影响评论保存）
        tempImageService.markImagesAsLinked(newImageUrls);
        
        // 处理被删除的图片：移除关联关系，让定时任务清理
        for (String oldUrl : oldImageUrls) {
            if (!newImageUrls.contains(oldUrl)) {
                tempImageService.unlinkImage(oldUrl);
            }
        }

        ReviewResponse response = convertToResponse(review, userId);

        // 查询并设置用户信息
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            response.setUserNickname(user.getNickname());
            response.setUserAvatar(resolveAvatarUrl(user.getAvatar()));
        }

        return response;
    }

    @Transactional
    public ReviewResponse submitReply(SubmitReviewRequest request, Long userId) {
        // 检查父评论是否存在
        ToolReview parentReview = reviewRepository.findById(request.getParentId())
                .orElseThrow(() -> new BusinessException("评论不存在"));

        // 只能回复评论（REVIEW），不能回复回复（REPLY）
        if (!parentReview.isReview()) {
            throw new BusinessException("只能回复评论，不能回复回复");
        }

        // 创建回复（标记为回复类型，不带评分）
        ToolReview reply = ToolReview.builder()
                .toolId(request.getToolId())
                .userId(userId)
                .parentId(request.getParentId())
                .content(request.getContent())
                .reviewType(ReviewType.REPLY)
                .status(ReviewStatus.ENABLED)
                .build();

        reviewRepository.save(reply);

        // 增加父评论的回复数
        parentReview.incrementReplyCount();
        reviewRepository.save(parentReview);

        ReviewResponse response = convertToResponse(reply, userId);

        // 查询并设置用户信息
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            response.setUserNickname(user.getNickname());
            response.setUserAvatar(resolveAvatarUrl(user.getAvatar()));
        }

        return response;
    }

    @Override
    public Page<ReviewResponse> getToolReviews(Long toolId, String sort, Pageable pageable, Long currentUserId) {
        Page<ToolReview> reviews;
        if ("hottest".equals(sort)) {
            reviews = reviewRepository.findReviewsByHot(toolId, pageable);
        } else {
            reviews = reviewRepository.findReviewsByNewest(toolId, pageable);
        }

        // 获取用户ID集合
        Set<Long> userIds = reviews.getContent().stream()
                .map(ToolReview::getUserId)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询当前用户的点赞状态
        Set<Long> likedReviewIds = currentUserId != null ?
                reviews.getContent().stream()
                        .map(ToolReview::getId)
                        .filter(id -> likeRepository.existsByReviewIdAndUserId(id, currentUserId))
                        .collect(Collectors.toSet()) :
                Set.of();

        return reviews.map(review -> {
            ReviewResponse response = convertToResponse(review, currentUserId);

            // 设置用户信息
            User user = userMap.get(review.getUserId());
            if (user != null) {
                response.setUserNickname(user.getNickname());
                response.setUserAvatar(resolveAvatarUrl(user.getAvatar()));
                response.setIsVip(user.getIsVip() != null && user.getIsVip());
                response.setIsAdmin(user.getRoles().stream()
                        .anyMatch(role -> "ADMIN".equals(role.getRole())));
            }

            // 设置点赞状态
            response.setIsLiked(likedReviewIds.contains(review.getId()));

            // 加载前3条回复
            if (review.getReplyCount() > 0) {
                List<ToolReview> replies = reviewRepository.findTopReplies(review.getId(), PageRequest.of(0, 3));

                // 获取回复的用户ID集合
                Set<Long> replyUserIds = replies.stream()
                        .map(ToolReview::getUserId)
                        .collect(Collectors.toSet());

                // 批量查询回复用户信息
                Map<Long, User> replyUserMap = userRepository.findAllById(replyUserIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

                response.setReplies(replies.stream()
                        .map(r -> {
                            ReviewResponse replyResponse = convertToResponse(r, currentUserId);
                            User replyUser = replyUserMap.get(r.getUserId());
                            if (replyUser != null) {
                                replyResponse.setUserNickname(replyUser.getNickname());
                                replyResponse.setUserAvatar(resolveAvatarUrl(replyUser.getAvatar()));
                                replyResponse.setIsVip(replyUser.getIsVip() != null && replyUser.getIsVip());
                                replyResponse.setIsAdmin(replyUser.getRoles().stream()
                                        .anyMatch(role -> "ADMIN".equals(role.getRole())));
                            }
                            return replyResponse;
                        })
                        .toList());
            }

            return response;
        });
    }

    @Override
    public List<ReviewResponse> getReplies(Long reviewId, Long currentUserId) {
        List<ToolReview> replies = reviewRepository.findReplies(reviewId);

        // 获取用户ID集合
        Set<Long> userIds = replies.stream()
                .map(ToolReview::getUserId)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return replies.stream()
                .map(reply -> {
                    ReviewResponse response = convertToResponse(reply, currentUserId);
                    User user = userMap.get(reply.getUserId());
                    if (user != null) {
                        response.setUserNickname(user.getNickname());
                        response.setUserAvatar(resolveAvatarUrl(user.getAvatar()));
                        response.setIsVip(user.getIsVip() != null && user.getIsVip());
                        response.setIsAdmin(user.getRoles().stream()
                                .anyMatch(role -> "ADMIN".equals(role.getRole())));
                    }
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public void toggleLike(Long reviewId, Long userId) {
        ToolReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        boolean hasLiked = likeRepository.existsByReviewIdAndUserId(reviewId, userId);

        if (hasLiked) {
            // 取消点赞
            likeRepository.deleteByReviewIdAndUserId(reviewId, userId);
            review.decrementLikeCount();
        } else {
            // 点赞
            ToolReviewLike like = ToolReviewLike.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .build();
            likeRepository.save(like);
            review.incrementLikeCount();
        }

        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        ToolReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        // 只能删除自己的评论
        if (!Objects.equals(review.getUserId(), userId)) {
            throw new BusinessException("只能删除自己的评论");
        }

        // 软删除
        review.setStatus(ReviewStatus.DISABLED);
        reviewRepository.save(review);

        // 如果是回复，减少父评论的回复数
        if (review.getParentId() != null) {
            reviewRepository.findById(review.getParentId()).ifPresent(parent -> {
                parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
                reviewRepository.save(parent);
            });
        }
    }

    @Override
    public ReviewStatsResponse getReviewStats(Long toolId) {
        ReviewStatsResponse stats = new ReviewStatsResponse();
        stats.setToolId(toolId);

        // 平均评分（只统计评论）
        Double avgRating = reviewRepository.calculateAverageRating(toolId);
        stats.setAverageRating(avgRating != null ?
                Math.round(avgRating * 10) / 10.0 : 0.0);

        // 总评论数（只统计评论，用于计算占比）
        long totalCount = reviewRepository.countRatedReviews(toolId);
        stats.setTotalCount((int) totalCount);

        // 各星级统计（只统计评论）
        List<Object[]> ratingCounts = reviewRepository.countByRating(toolId);
        for (Object[] rc : ratingCounts) {
            Integer rating = (Integer) rc[0];
            Long count = (Long) rc[1];
            switch (rating) {
                case 5 -> stats.setFiveStarCount(count.intValue());
                case 4 -> stats.setFourStarCount(count.intValue());
                case 3 -> stats.setThreeStarCount(count.intValue());
                case 2 -> stats.setTwoStarCount(count.intValue());
                case 1 -> stats.setOneStarCount(count.intValue());
            }
        }

        return stats;
    }

    @Override
    public boolean hasReviewed(Long toolId, Long userId) {
        return reviewRepository.existsByToolIdAndUserIdAndReviewType(toolId, userId, ReviewType.REVIEW);
    }

    /**
     * 转换为响应对象
     */
    private ReviewResponse convertToResponse(ToolReview review, Long currentUserId) {
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
                response.setImageUrls(objectMapper.readValue(review.getImageUrls(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            } catch (JsonProcessingException e) {
                log.warn("解析图片URL失败: {}", e.getMessage());
            }
        }

        // 检查当前用户是否点赞
        if (currentUserId != null) {
            response.setIsLiked(likeRepository.existsByReviewIdAndUserId(review.getId(), currentUserId));
            response.setIsOwner(Objects.equals(review.getUserId(), currentUserId));
        } else {
            response.setIsLiked(false);
            response.setIsOwner(false);
        }

        return response;
    }

    /**
     * 转换图片URL列表为JSON字符串
     */
    private String convertImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (JsonProcessingException e) {
            log.warn("转换图片URL失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析JSON字符串为图片URL列表
     */
    private List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(imageUrlsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.warn("解析图片URL失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 解析头像URL，如果是相对路径则添加baseUrl
     */
    private String resolveAvatarUrl(String avatar) {
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
