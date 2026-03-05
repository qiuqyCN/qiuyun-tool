package dev.qiuyun.qiuyuntoolbackend.scheduler;

import dev.qiuyun.qiuyuntoolbackend.entity.ToolFile;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolFileRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final ToolTaskRepository taskRepository;
    private final ToolFileRepository fileRepository;

    @Value("${tool.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Scheduled(cron = "${tool.cleanup.cron:0 0 */1 * * ?}")
    public void cleanupExpiredFiles() {
        if (!cleanupEnabled) {
            return;
        }

        log.info("Starting cleanup of expired files and tasks...");

        LocalDateTime now = LocalDateTime.now();

        List<ToolFile> expiredFiles = fileRepository.findByExpireAtBefore(now);

        int deletedFiles = 0;
        for (ToolFile file : expiredFiles) {
            try {
                Path filePath = Paths.get(file.getStoragePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                fileRepository.delete(file);
                deletedFiles++;
            } catch (Exception e) {
                log.warn("Failed to delete file: {}", file.getFileId(), e);
            }
        }

        int deletedTasks = taskRepository.deleteByExpireAtBefore(now);

        log.info("Cleanup completed. Deleted {} files and {} tasks", deletedFiles, deletedTasks);
    }
}
