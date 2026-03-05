package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ToolFileRepository extends JpaRepository<ToolFile, Long> {

    Optional<ToolFile> findByFileId(String fileId);

    Optional<ToolFile> findByStoragePath(String storagePath);

    List<ToolFile> findByTaskId(String taskId);

    List<ToolFile> findByExpireAtBefore(LocalDateTime expireAt);

    @Modifying
    @Query("DELETE FROM ToolFile f WHERE f.expireAt < :expireAt")
    int deleteByExpireAtBefore(LocalDateTime expireAt);

    @Modifying
    @Query("DELETE FROM ToolFile f WHERE f.taskId = :taskId")
    int deleteByTaskId(String taskId);
}
