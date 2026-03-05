package dev.qiuyun.qiuyuntoolbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import dev.qiuyun.qiuyuntoolbackend.enums.TaskStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_tasks", indexes = {
        @Index(name = "idx_task_id", columnList = "task_id", unique = true),
        @Index(name = "idx_user_status", columnList = "user_id, status"),
        @Index(name = "idx_expire", columnList = "expire_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, unique = true, length = 64)
    private String taskId;

    @Column(name = "tool_code", nullable = false, length = 100)
    private String toolCode;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column
    @Builder.Default
    private Integer progress = 0;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object inputParams;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object outputResult;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    @PrePersist
    public void prePersist() {
        if (expireAt == null) {
            expireAt = LocalDateTime.now().plusDays(1);
        }
    }
}
