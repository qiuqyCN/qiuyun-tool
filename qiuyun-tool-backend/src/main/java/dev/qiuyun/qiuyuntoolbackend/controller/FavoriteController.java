package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.security.CurrentUser;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 切换收藏状态（收藏/取消收藏）
     *
     * @param toolId 工具ID
     * @param user   当前登录用户
     * @return 切换后的收藏状态
     */
    @PostMapping("/toggle/{toolId}")
    public ApiResponse<Map<String, Object>> toggleFavorite(
            @PathVariable Long toolId,
            @CurrentUser UserDetailsImpl user) {

        if (user == null) {
            return ApiResponse.error(401, "请先登录");
        }

        boolean isFavorite = favoriteService.toggleFavorite(user.getId(), toolId);
        long favoriteCount = favoriteService.getFavoriteCount(toolId);

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", isFavorite);
        result.put("favoriteCount", favoriteCount);

        return ApiResponse.success(result);
    }

    /**
     * 收藏工具
     *
     * @param toolId 工具ID
     * @param user   当前登录用户
     * @return 操作结果
     */
    @PostMapping("/{toolId}")
    public ApiResponse<Map<String, Object>> addFavorite(
            @PathVariable Long toolId,
            @CurrentUser UserDetailsImpl user) {

        if (user == null) {
            return ApiResponse.error(401, "请先登录");
        }

        favoriteService.addFavorite(user.getId(), toolId);
        long favoriteCount = favoriteService.getFavoriteCount(toolId);

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", true);
        result.put("favoriteCount", favoriteCount);

        return ApiResponse.success(result);
    }

    /**
     * 取消收藏
     *
     * @param toolId 工具ID
     * @param user   当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/{toolId}")
    public ApiResponse<Map<String, Object>> removeFavorite(
            @PathVariable Long toolId,
            @CurrentUser UserDetailsImpl user) {

        if (user == null) {
            return ApiResponse.error(401, "请先登录");
        }

        favoriteService.removeFavorite(user.getId(), toolId);
        long favoriteCount = favoriteService.getFavoriteCount(toolId);

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", false);
        result.put("favoriteCount", favoriteCount);

        return ApiResponse.success(result);
    }

    /**
     * 检查用户是否已收藏该工具
     *
     * @param toolId 工具ID
     * @param user   当前登录用户（可选）
     * @return 是否已收藏
     */
    @GetMapping("/check/{toolId}")
    public ApiResponse<Map<String, Object>> checkFavorite(
            @PathVariable Long toolId,
            @CurrentUser(required = false) UserDetailsImpl user) {

        boolean isFavorite = false;
        if (user != null) {
            isFavorite = favoriteService.isFavorite(user.getId(), toolId);
        }

        long favoriteCount = favoriteService.getFavoriteCount(toolId);

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", isFavorite);
        result.put("favoriteCount", favoriteCount);

        return ApiResponse.success(result);
    }

    /**
     * 批量检查收藏状态
     *
     * @param toolIds 工具ID列表
     * @param user    当前登录用户（可选）
     * @return 收藏状态映射
     */
    @PostMapping("/batch-check")
    public ApiResponse<Map<String, Object>> batchCheckFavorite(
            @RequestBody List<Long> toolIds,
            @CurrentUser(required = false) UserDetailsImpl user) {

        Map<Long, Boolean> favoriteMap = new HashMap<>();
        if (user != null && toolIds != null && !toolIds.isEmpty()) {
            favoriteMap = favoriteService.batchCheckFavorite(user.getId(), toolIds);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("favorites", favoriteMap);

        return ApiResponse.success(result);
    }

    /**
     * 获取用户的收藏列表
     *
     * @param page 页码
     * @param size 每页数量
     * @param user 当前登录用户
     * @return 收藏的工具列表
     */
    @GetMapping
    public ApiResponse<Page<ToolResponse>> getUserFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser UserDetailsImpl user) {

        if (user == null) {
            return ApiResponse.error(401, "请先登录");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Tool> toolPage = favoriteService.getUserFavorites(user.getId(), pageable);

        Page<ToolResponse> responsePage = toolPage.map(this::convertToToolResponse);

        return ApiResponse.success(responsePage);
    }

    /**
     * 获取用户的收藏数量
     *
     * @param user 当前登录用户
     * @return 收藏数量
     */
    @GetMapping("/count")
    public ApiResponse<Map<String, Long>> getUserFavoriteCount(
            @CurrentUser UserDetailsImpl user) {

        if (user == null) {
            return ApiResponse.error(401, "请先登录");
        }

        long count = favoriteService.getUserFavoriteCount(user.getId());

        Map<String, Long> result = new HashMap<>();
        result.put("count", count);

        return ApiResponse.success(result);
    }

    /**
     * 获取工具的收藏数
     *
     * @param toolId 工具ID
     * @return 收藏数
     */
    @GetMapping("/count/{toolId}")
    public ApiResponse<Map<String, Long>> getToolFavoriteCount(@PathVariable Long toolId) {
        long count = favoriteService.getFavoriteCount(toolId);

        Map<String, Long> result = new HashMap<>();
        result.put("count", count);

        return ApiResponse.success(result);
    }

    /**
     * 转换Tool为ToolResponse
     */
    private ToolResponse convertToToolResponse(Tool tool) {
        return ToolResponse.builder()
                .id(tool.getId())
                .code(tool.getCode())
                .name(tool.getName())
                .description(tool.getDescription())
                .category(tool.getCategory() != null ? tool.getCategory().getCode() : null)
                .icon(tool.getIcon())
                .iconColor(tool.getIconColor())
                .iconBgColor(tool.getIconBgColor())
                .isVip(tool.getIsVip())
                .isHot(tool.getIsHot())
                .rating(tool.getRating())
                .viewCount(tool.getViewCount())
                .usageCount(tool.getUsageCount())
                .favoriteCount(tool.getFavoriteCount())
                .build();
    }
}
