package dev.qiuyun.qiuyuntoolbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工具排行榜实体
 */
@Entity
@Table(name = "tool_rankings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "ranking_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RankingType rankingType;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "visit_count")
    private Integer visitCount;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "favorite_count")
    private Integer favoriteCount;

    @Column(name = "hot_score")
    private Double hotScore;

    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 排行榜类型
     */
    public enum RankingType {
        REALTIME,  // 实时榜
        DAILY,     // 日榜
        WEEKLY,    // 周榜
        MONTHLY    // 月榜
    }
}
