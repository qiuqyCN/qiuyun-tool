package dev.qiuyun.qiuyuntoolbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 工具访问日志实体
 */
@Entity
@Table(name = "tool_access_logs", indexes = {
        // 复合索引：查询工具的访问统计
        // 覆盖场景：统计工具访问量 - WHERE tool_id=? AND action_type=? AND created_at>=?
        @Index(name = "idx_access_log_tool_action_time", columnList = "tool_id, action_type, created_at"),
        // 复合索引：查询用户的访问历史
        // 覆盖场景：查询用户最近访问的工具 - WHERE user_id=? ORDER BY created_at DESC
        @Index(name = "idx_access_log_user_time", columnList = "user_id, created_at"),
        // 索引：按时间清理旧日志
        // 覆盖场景：定时清理过期日志 - WHERE created_at<?
        @Index(name = "idx_access_log_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "action_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 操作类型
     */
    public enum ActionType {
        VIEW,      // 浏览
        USE,       // 使用
        FAVORITE   // 收藏
    }
}
