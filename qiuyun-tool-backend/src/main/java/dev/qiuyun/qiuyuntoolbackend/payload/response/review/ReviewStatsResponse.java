package dev.qiuyun.qiuyuntoolbackend.payload.response.review;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论统计响应
 */
@Data
public class ReviewStatsResponse {

    private Long toolId;
    private Double averageRating;
    private Integer totalCount;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;

    /**
     * 获取各星级占比
     */
    public Map<Integer, Double> getRatingPercentages() {
        Map<Integer, Double> percentages = new HashMap<>();
        if (totalCount == null || totalCount == 0) {
            percentages.put(5, 0.0);
            percentages.put(4, 0.0);
            percentages.put(3, 0.0);
            percentages.put(2, 0.0);
            percentages.put(1, 0.0);
            return percentages;
        }

        percentages.put(5, calculatePercentage(fiveStarCount));
        percentages.put(4, calculatePercentage(fourStarCount));
        percentages.put(3, calculatePercentage(threeStarCount));
        percentages.put(2, calculatePercentage(twoStarCount));
        percentages.put(1, calculatePercentage(oneStarCount));
        return percentages;
    }

    private Double calculatePercentage(Integer count) {
        if (count == null || count == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(count * 100.0 / totalCount)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
