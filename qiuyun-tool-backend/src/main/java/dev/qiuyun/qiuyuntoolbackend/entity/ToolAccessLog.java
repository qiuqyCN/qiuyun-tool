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
@Table(name = "tool_access_logs")
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
