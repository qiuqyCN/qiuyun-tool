package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private String icon;
    private String iconColor;
    private String iconBgColor;
    private Boolean isVip;
    private Boolean isHot;
    private String priceMode;
    private Long visits;
    private Long viewCount;
    private Long usageCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer favoriteCount;
    private String instructions;
    private List<String> tags;
    private LocalDateTime createdAt;
}
