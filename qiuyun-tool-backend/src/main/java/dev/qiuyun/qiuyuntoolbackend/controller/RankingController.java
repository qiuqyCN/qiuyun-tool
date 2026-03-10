package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolRanking;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ranking.RankingItemResponse;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.security.CurrentUser;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.RankingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排行榜控制器
 */
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final ToolRepository toolRepository;

    /**
     * 获取排行榜
     *
     * @param type   排行榜类型: realtime, daily, weekly, monthly
     * @param date   日期 (可选，默认今天)
     * @param limit  返回数量 (可选，默认10)
     * @return 排行榜列表
     */
    @GetMapping
    public ApiResponse<List<RankingItemResponse>> getRankings(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {

        if (date == null) {
            date = LocalDate.now();
        }

        List<ToolRanking> rankings;
        switch (type.toLowerCase()) {
            case "realtime":
                rankings = rankingService.getRealtimeRanking(limit);
                break;
            case "daily":
                rankings = rankingService.getDailyRanking(date, limit);
                break;
            case "weekly":
                rankings = rankingService.getWeeklyRanking(date, limit);
                break;
            case "monthly":
                rankings = rankingService.getMonthlyRanking(date, limit);
                break;
            default:
                return ApiResponse.error(400, "不支持的排行榜类型: " + type);
        }

        // 获取工具信息并组装响应
        List<Long> toolIds = rankings.stream()
                .map(ToolRanking::getToolId)
                .collect(Collectors.toList());

        Map<Long, Tool> toolMap = toolRepository.findAllById(toolIds)
                .stream()
                .collect(Collectors.toMap(Tool::getId, t -> t));

        List<RankingItemResponse> responses = rankings.stream()
                .map(ranking -> {
                    Tool tool = toolMap.get(ranking.getToolId());
                    if (tool == null) {
                        return null;
                    }
                    return RankingItemResponse.builder()
                            .rank(ranking.getRanking())
                            .toolId(ranking.getToolId())
                            .toolCode(tool.getCode())
                            .toolName(tool.getName())
                            .description(tool.getDescription())
                            .icon(tool.getIcon())
                            .iconColor(tool.getIconColor())
                            .iconBgColor(tool.getIconBgColor())
                            .category(tool.getCategory() != null ? tool.getCategory().getCode() : null)
                            .isVip(tool.getIsVip())
                            .rating(tool.getRating())
                            .visitCount(ranking.getVisitCount())
                            .usageCount(ranking.getUsageCount())
                            .favoriteCount(ranking.getFavoriteCount())
                            .hotScore(ranking.getHotScore())
                            .build();
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * 记录工具访问
     *
     * @param toolId  工具ID
     * @param action  动作类型: view, use, favorite
     * @param request HTTP请求
     * @param user    当前登录用户（可选）
     * @return 操作结果
     */
    @PostMapping("/record/{toolId}")
    public ApiResponse<Void> recordAccess(
            @PathVariable Long toolId,
            @RequestParam String action,
            HttpServletRequest request,
            @CurrentUser(required = false) UserDetailsImpl user) {

        // 获取用户ID（如果已登录）
        Long userId = user != null ? user.getId() : null;

        // 获取IP地址
        String ipAddress = getClientIpAddress(request);

        // 转换action类型
        dev.qiuyun.qiuyuntoolbackend.entity.ToolAccessLog.ActionType actionType;
        try {
            actionType = dev.qiuyun.qiuyuntoolbackend.entity.ToolAccessLog.ActionType
                    .valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, "不支持的动作类型: " + action);
        }

        rankingService.recordAccess(toolId, userId, actionType, ipAddress);

        return ApiResponse.success(null);
    }

    /**
     * 手动触发排行榜计算（仅管理员）
     *
     * @param type 排行榜类型
     * @param date 日期
     * @return 操作结果
     */
    @PostMapping("/calculate")
    public ApiResponse<String> calculateRanking(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        switch (type.toLowerCase()) {
            case "realtime":
                rankingService.calculateRealtimeRanking();
                return ApiResponse.success("实时榜计算完成");
            case "daily":
                rankingService.calculateDailyRanking(date);
                return ApiResponse.success("日榜计算完成: " + date);
            case "weekly":
                rankingService.calculateWeeklyRanking(date);
                return ApiResponse.success("周榜计算完成: " + date);
            case "monthly":
                rankingService.calculateMonthlyRanking(date);
                return ApiResponse.success("月榜计算完成: " + date);
            default:
                return ApiResponse.error(400, "不支持的排行榜类型: " + type);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
