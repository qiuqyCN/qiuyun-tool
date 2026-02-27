package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private Boolean isVip;
    private Long visits;
    private BigDecimal rating;
    private Integer reviewCount;
    private List<String> tags;
}
