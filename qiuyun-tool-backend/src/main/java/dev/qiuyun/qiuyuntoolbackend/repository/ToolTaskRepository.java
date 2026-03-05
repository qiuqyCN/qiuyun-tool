package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolTask;
import dev.qiuyun.qiuyuntoolbackend.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ToolTaskRepository extends JpaRepository<ToolTask, Long> {

    Optional<ToolTask> findByTaskId(String taskId);

    List<ToolTask> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ToolTask> findByStatusAndExpireAtBefore(TaskStatus status, LocalDateTime expireAt);

    List<ToolTask> findByExpireAtBefore(LocalDateTime expireAt);

    @Modifying
    @Query("DELETE FROM ToolTask t WHERE t.expireAt < :expireAt")
    int deleteByExpireAtBefore(LocalDateTime expireAt);

    @Modifying
    @Query("UPDATE ToolTask t SET t.status = :status, t.progress = :progress WHERE t.taskId = :taskId")
    void updateStatusAndProgress(String taskId, TaskStatus status, Integer progress);

    @Modifying
    @Query("UPDATE ToolTask t SET t.status = :status, t.progress = :progress, t.outputResult = :result, t.completedAt = CURRENT_TIMESTAMP WHERE t.taskId = :taskId")
    void updateCompleted(String taskId, TaskStatus status, Integer progress, Object result);

    @Modifying
    @Query("UPDATE ToolTask t SET t.status = :status, t.errorMessage = :errorMessage WHERE t.taskId = :taskId")
    void updateFailed(String taskId, TaskStatus status, String errorMessage);
}
