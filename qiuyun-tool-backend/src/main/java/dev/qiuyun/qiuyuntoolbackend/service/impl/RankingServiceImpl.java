package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolAccessLog;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolRanking;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolAccessLogRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRankingRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingServiceImpl implements RankingService {

    private final ToolAccessLogRepository accessLogRepository;
    private final ToolRankingRepository rankingRepository;
    private final ToolRepository toolRepository;

    // 热度分计算权重
    private static final double USAGE_WEIGHT = 0.6;
    private static final double VIEW_WEIGHT = 0.3;
    private static final double FAVORITE_WEIGHT = 0.1;

    @Override
    @Transactional
    public void recordAccess(Long toolId, Long userId, ToolAccessLog.ActionType actionType, String ipAddress) {
        ToolAccessLog log = ToolAccessLog.builder()
                .toolId(toolId)
                .userId(userId)
                .actionType(actionType)
                .ipAddress(ipAddress)
                .build();
        accessLogRepository.save(log);
    }

    @Override
    public List<ToolRanking> getRealtimeRanking(int limit) {
        // 获取今天的实时榜
        LocalDate today = LocalDate.now();
        List<ToolRanking> rankings = rankingRepository.findByRankingTypeAndStatDateOrderByRankingAsc(
                ToolRanking.RankingType.REALTIME, today);
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<ToolRanking> getDailyRanking(LocalDate date, int limit) {
        List<ToolRanking> rankings = rankingRepository.findByRankingTypeAndStatDateOrderByRankingAsc(
                ToolRanking.RankingType.DAILY, date);
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<ToolRanking> getWeeklyRanking(LocalDate date, int limit) {
        // 获取该日期所在周的数据
        LocalDate weekStart = date.with(WeekFields.ISO.dayOfWeek(), 1);
        List<ToolRanking> rankings = rankingRepository.findByRankingTypeAndStatDateOrderByRankingAsc(
                ToolRanking.RankingType.WEEKLY, weekStart);
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<ToolRanking> getMonthlyRanking(LocalDate date, int limit) {
        // 获取该日期所在月的数据
        LocalDate monthStart = date.withDayOfMonth(1);
        List<ToolRanking> rankings = rankingRepository.findByRankingTypeAndStatDateOrderByRankingAsc(
                ToolRanking.RankingType.MONTHLY, monthStart);
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void calculateDailyRanking(LocalDate date) {
        log.info("开始计算日榜: {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

        calculateAndSaveRanking(ToolRanking.RankingType.DAILY, date, startOfDay, endOfDay);

        log.info("日榜计算完成: {}", date);
    }

    @Override
    @Transactional
    public void calculateWeeklyRanking(LocalDate date) {
        log.info("开始计算周榜: {}", date);

        // 获取该日期所在周的起止时间
        LocalDate weekStart = date.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        LocalDateTime startOfWeek = weekStart.atStartOfDay();
        LocalDateTime endOfWeek = weekEnd.plusDays(1).atStartOfDay().minusNanos(1);

        calculateAndSaveRanking(ToolRanking.RankingType.WEEKLY, weekStart, startOfWeek, endOfWeek);

        log.info("周榜计算完成: {} - {}", weekStart, weekEnd);
    }

    @Override
    @Transactional
    public void calculateMonthlyRanking(LocalDate date) {
        log.info("开始计算月榜: {}", date);

        // 获取该日期所在月的起止时间
        LocalDate monthStart = date.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        LocalDateTime startOfMonth = monthStart.atStartOfDay();
        LocalDateTime endOfMonth = monthEnd.plusDays(1).atStartOfDay().minusNanos(1);

        calculateAndSaveRanking(ToolRanking.RankingType.MONTHLY, monthStart, startOfMonth, endOfMonth);

        log.info("月榜计算完成: {} - {}", monthStart, monthEnd);
    }

    @Override
    @Transactional
    public void calculateRealtimeRanking() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 先删除今天的实时榜数据
        List<ToolRanking> existingRankings = rankingRepository.findByRankingTypeAndStatDateOrderByRankingAsc(
                ToolRanking.RankingType.REALTIME, today);
        rankingRepository.deleteAll(existingRankings);

        // 重新计算今天的实时榜（从当天开始到现在）
        LocalDateTime startOfDay = today.atStartOfDay();
        calculateAndSaveRanking(ToolRanking.RankingType.REALTIME, today, startOfDay, now);

        log.info("实时榜计算完成: {}", today);
    }

    /**
     * 计算并保存排行榜
     */
    private void calculateAndSaveRanking(ToolRanking.RankingType rankingType, LocalDate statDate,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        // 获取所有工具
        List<Tool> tools = toolRepository.findByIsActiveTrue();

        // 获取访问统计数据
        List<ToolAccessLogRepository.ToolAccessStats> stats =
                accessLogRepository.findStatsByTimeRange(startTime, endTime);

        // 转换为Map方便查询
        Map<Long, ToolAccessLogRepository.ToolAccessStats> statsMap = stats.stream()
                .collect(Collectors.toMap(
                        ToolAccessLogRepository.ToolAccessStats::getToolId,
                        s -> s,
                        (s1, s2) -> s1
                ));

        // 计算每个工具的热度分
        List<ToolRanking> rankings = new ArrayList<>();
        for (Tool tool : tools) {
            ToolAccessLogRepository.ToolAccessStats stat = statsMap.get(tool.getId());

            long viewCount = stat != null ? stat.getViewCount() : 0;
            long usageCount = stat != null ? stat.getUsageCount() : 0;
            long favoriteCount = stat != null ? stat.getFavoriteCount() : 0;

            // 计算热度分: usage*0.6 + view*0.3 + favorite*0.1
            double hotScore = usageCount * USAGE_WEIGHT +
                    viewCount * VIEW_WEIGHT +
                    favoriteCount * FAVORITE_WEIGHT;

            ToolRanking ranking = ToolRanking.builder()
                    .toolId(tool.getId())
                    .rankingType(rankingType)
                    .statDate(statDate)
                    .visitCount((int) viewCount)
                    .usageCount((int) usageCount)
                    .favoriteCount((int) favoriteCount)
                    .hotScore(hotScore)
                    .ranking(0) // 临时值，后面排序后设置
                    .build();

            rankings.add(ranking);
        }

        // 按热度分排序
        rankings.sort((a, b) -> Double.compare(b.getHotScore(), a.getHotScore()));

        // 设置排名并保存
        int rank = 1;
        for (ToolRanking ranking : rankings) {
            ranking.setRanking(rank++);

            // 检查是否已存在记录
            Optional<ToolRanking> existing = rankingRepository.findByToolIdAndRankingTypeAndStatDate(
                    ranking.getToolId(), rankingType, statDate);

            if (existing.isPresent()) {
                // 更新现有记录
                ToolRanking existingRanking = existing.get();
                existingRanking.setVisitCount(ranking.getVisitCount());
                existingRanking.setUsageCount(ranking.getUsageCount());
                existingRanking.setFavoriteCount(ranking.getFavoriteCount());
                existingRanking.setHotScore(ranking.getHotScore());
                existingRanking.setRanking(ranking.getRanking());
                rankingRepository.save(existingRanking);
            } else {
                // 创建新记录
                rankingRepository.save(ranking);
            }
        }
    }
}
