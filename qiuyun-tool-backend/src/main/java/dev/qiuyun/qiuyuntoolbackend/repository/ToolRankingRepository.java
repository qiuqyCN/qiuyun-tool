package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 工具排行榜数据访问层
 */
@Repository
public interface ToolRankingRepository extends JpaRepository<ToolRanking, Long> {

    /**
     * 查询指定类型和日期的排行榜
     */
    List<ToolRanking> findByRankingTypeAndStatDateOrderByRankingAsc(ToolRanking.RankingType rankingType, LocalDate statDate);

    /**
     * 查询指定类型、日期和排名的记录
     */
    Optional<ToolRanking> findByToolIdAndRankingTypeAndStatDate(Long toolId, ToolRanking.RankingType rankingType, LocalDate statDate);

    /**
     * 查询指定工具在指定类型和日期范围内的排名历史
     */
    List<ToolRanking> findByToolIdAndRankingTypeAndStatDateBetweenOrderByStatDateAsc(
            Long toolId, ToolRanking.RankingType rankingType, LocalDate startDate, LocalDate endDate);

    /**
     * 查询最新的排行榜数据
     */
    @Query("SELECT tr FROM ToolRanking tr WHERE tr.rankingType = :rankingType " +
           "AND tr.statDate = (SELECT MAX(tr2.statDate) FROM ToolRanking tr2 WHERE tr2.rankingType = :rankingType) " +
           "ORDER BY tr.ranking ASC")
    List<ToolRanking> findLatestByRankingType(@Param("rankingType") ToolRanking.RankingType rankingType);

    /**
     * 删除指定日期之前的旧数据
     */
    void deleteByStatDateBefore(LocalDate date);
}
