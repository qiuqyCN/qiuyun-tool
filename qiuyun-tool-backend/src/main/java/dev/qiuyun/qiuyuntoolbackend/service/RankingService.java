package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolAccessLog;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolRanking;

import java.time.LocalDate;
import java.util.List;

/**
 * 排行榜服务接口
 */
public interface RankingService {

    /**
     * 记录工具访问日志
     */
    void recordAccess(Long toolId, Long userId, ToolAccessLog.ActionType actionType, String ipAddress);

    /**
     * 获取实时排行榜
     */
    List<ToolRanking> getRealtimeRanking(int limit);

    /**
     * 获取日榜
     */
    List<ToolRanking> getDailyRanking(LocalDate date, int limit);

    /**
     * 获取周榜
     */
    List<ToolRanking> getWeeklyRanking(LocalDate date, int limit);

    /**
     * 获取月榜
     */
    List<ToolRanking> getMonthlyRanking(LocalDate date, int limit);

    /**
     * 计算日榜
     */
    void calculateDailyRanking(LocalDate date);

    /**
     * 计算周榜
     */
    void calculateWeeklyRanking(LocalDate date);

    /**
     * 计算月榜
     */
    void calculateMonthlyRanking(LocalDate date);

    /**
     * 计算实时榜
     */
    void calculateRealtimeRanking();
}
