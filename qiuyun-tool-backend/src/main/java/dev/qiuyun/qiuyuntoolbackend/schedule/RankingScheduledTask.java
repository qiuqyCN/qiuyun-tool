package dev.qiuyun.qiuyuntoolbackend.schedule;

import dev.qiuyun.qiuyuntoolbackend.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 排行榜定时任务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduledTask {

    private final RankingService rankingService;

    /**
     * 计算实时榜 - 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void calculateRealtimeRanking() {
        log.info("开始定时任务: 计算实时榜");
        try {
            rankingService.calculateRealtimeRanking();
            log.info("定时任务完成: 实时榜计算成功");
        } catch (Exception e) {
            log.error("定时任务失败: 实时榜计算出错", e);
        }
    }

    /**
     * 计算日榜 - 每天00:30执行
     */
    @Scheduled(cron = "0 30 0 * * *")
    public void calculateDailyRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始定时任务: 计算日榜, 日期: {}", yesterday);
        try {
            rankingService.calculateDailyRanking(yesterday);
            log.info("定时任务完成: 日榜计算成功, 日期: {}", yesterday);
        } catch (Exception e) {
            log.error("定时任务失败: 日榜计算出错, 日期: {}", yesterday, e);
        }
    }

    /**
     * 计算周榜 - 每周一01:00执行
     */
    @Scheduled(cron = "0 0 1 * * MON")
    public void calculateWeeklyRanking() {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        log.info("开始定时任务: 计算周榜, 日期: {}", lastWeek);
        try {
            rankingService.calculateWeeklyRanking(lastWeek);
            log.info("定时任务完成: 周榜计算成功, 日期: {}", lastWeek);
        } catch (Exception e) {
            log.error("定时任务失败: 周榜计算出错, 日期: {}", lastWeek, e);
        }
    }

    /**
     * 计算月榜 - 每月1日02:00执行
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void calculateMonthlyRanking() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        log.info("开始定时任务: 计算月榜, 日期: {}", lastMonth);
        try {
            rankingService.calculateMonthlyRanking(lastMonth);
            log.info("定时任务完成: 月榜计算成功, 日期: {}", lastMonth);
        } catch (Exception e) {
            log.error("定时任务失败: 月榜计算出错, 日期: {}", lastMonth, e);
        }
    }
}
