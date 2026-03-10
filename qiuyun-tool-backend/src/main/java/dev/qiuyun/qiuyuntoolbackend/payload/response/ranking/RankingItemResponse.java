package dev.qiuyun.qiuyuntoolbackend.payload.response.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 排行榜单项响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingItemResponse {

    private Integer rank;
    private Long toolId;
    private String toolCode;
    private String toolName;
    private String description;
    private String icon;
    private String iconColor;
    private String iconBgColor;
    private String category;
    private Boolean isVip;
    private BigDecimal rating;

    private Integer visitCount;
    private Integer usageCount;
    private Integer favoriteCount;
    private Double hotScore;
}
