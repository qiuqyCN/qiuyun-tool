package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具访问日志数据访问层
 */
@Repository
public interface ToolAccessLogRepository extends JpaRepository<ToolAccessLog, Long> {

    /**
     * 统计指定时间范围内的工具访问数据
     */
    @Query("SELECT log.toolId as toolId, " +
           "SUM(CASE WHEN log.actionType = 'VIEW' THEN 1 ELSE 0 END) as viewCount, " +
           "SUM(CASE WHEN log.actionType = 'USE' THEN 1 ELSE 0 END) as usageCount, " +
           "SUM(CASE WHEN log.actionType = 'FAVORITE' THEN 1 ELSE 0 END) as favoriteCount " +
           "FROM ToolAccessLog log " +
           "WHERE log.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY log.toolId")
    List<ToolAccessStats> findStatsByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定工具在指定时间范围内的访问数据
     */
    @Query("SELECT log.toolId as toolId, " +
           "SUM(CASE WHEN log.actionType = 'VIEW' THEN 1 ELSE 0 END) as viewCount, " +
           "SUM(CASE WHEN log.actionType = 'USE' THEN 1 ELSE 0 END) as usageCount, " +
           "SUM(CASE WHEN log.actionType = 'FAVORITE' THEN 1 ELSE 0 END) as favoriteCount " +
           "FROM ToolAccessLog log " +
           "WHERE log.toolId = :toolId AND log.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY log.toolId")
    ToolAccessStats findStatsByToolIdAndTimeRange(@Param("toolId") Long toolId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 访问统计投影接口
     */
    interface ToolAccessStats {
        Long getToolId();
        Long getViewCount();
        Long getUsageCount();
        Long getFavoriteCount();
    }
}
