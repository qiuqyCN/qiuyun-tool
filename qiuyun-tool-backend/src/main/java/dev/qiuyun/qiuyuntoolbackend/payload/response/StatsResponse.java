package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计数据响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponse {

    /**
     * 工具总数
     */
    private Integer totalTools;

    /**
     * 日活跃用户
     */
    private Integer dailyActiveUsers;

    /**
     * 月访问量
     */
    private Long monthlyVisits;
}
