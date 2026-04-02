package dev.qiuyun.qiuyuntoolbackend.schedule;

import dev.qiuyun.qiuyuntoolbackend.repository.ToolTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

/**
 * 文件清理定时任务
 * 定期清理过期的任务文件和空目录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduledTask {

    private final ToolTaskRepository taskRepository;

    @Value("${tool.file.storage-dir:${user.home}/qiuyun-tool/files}")
    private String storageDirPath;

    @Value("${tool.file.retention-hours:24}")
    private int retentionHours;

    @Value("${tool.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * 日期格式器（yyyyMMdd）
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 每小时执行一次清理
     */
    @Scheduled(cron = "${tool.cleanup.cron:0 0 * * * ?}")
    public void cleanupExpiredFiles() {
        if (!cleanupEnabled) {
            log.debug("文件清理任务已禁用");
            return;
        }

        log.info("开始执行文件清理任务...");

        try {
            Path baseDir = Paths.get(storageDirPath);
            if (!Files.exists(baseDir)) {
                log.info("文件存储目录不存在，跳过清理: {}", baseDir.toAbsolutePath());
                return;
            }

            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(retentionHours);
            log.info("清理截止时间: {} (保留 {} 小时)", cutoffTime, retentionHours);

            int deletedDateDirs = 0;
            int deletedTaskDirs = 0;
            int deletedEmptyDateDirs = 0;

            // 遍历日期目录
            try (DirectoryStream<Path> dateDirs = Files.newDirectoryStream(baseDir)) {
                for (Path dateDir : dateDirs) {
                    if (!Files.isDirectory(dateDir)) {
                        continue;
                    }

                    // 解析日期目录名
                    String dirName = dateDir.getFileName().toString();
                    try {
                        LocalDateTime dirDate = LocalDateTime.parse(dirName + "000000",
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                        // 如果目录已过期，直接删除整个日期目录
                        if (dirDate.isBefore(cutoffTime)) {
                            deleteDirectory(dateDir);
                            deletedDateDirs++;
                            log.info("删除过期日期目录: {}", dateDir.toAbsolutePath());
                            continue;
                        }

                        // 未过期，检查并清理子目录
                        int deleted = cleanupDateDir(dateDir);
                        deletedTaskDirs += deleted;

                    } catch (DateTimeParseException e) {
                        log.warn("无法解析日期目录名: {}, 路径: {}", dirName, dateDir.toAbsolutePath());
                    }
                }
            }

            // 再次遍历，删除空的日期目录
            try (DirectoryStream<Path> dateDirs = Files.newDirectoryStream(baseDir)) {
                for (Path dateDir : dateDirs) {
                    if (!Files.isDirectory(dateDir)) {
                        continue;
                    }
                    if (isDirectoryEmpty(dateDir)) {
                        Files.delete(dateDir);
                        deletedEmptyDateDirs++;
                        log.info("删除空日期目录: {}", dateDir.toAbsolutePath());
                    }
                }
            }

            // 清理数据库中已过期的任务记录
            int deletedTasks = taskRepository.deleteByExpireAtBefore(LocalDateTime.now());

            log.info("文件清理任务完成。删除过期日期目录: {}, 删除空任务目录: {}, 删除空日期目录: {}, 删除过期任务记录: {}",
                    deletedDateDirs, deletedTaskDirs, deletedEmptyDateDirs, deletedTasks);

        } catch (Exception e) {
            log.error("文件清理任务执行失败", e);
        }
    }

    /**
     * 清理日期目录下的空任务目录
     * @param dateDir 日期目录路径
     * @return 删除的目录数量
     */
    private int cleanupDateDir(Path dateDir) throws IOException {
        int deletedCount = 0;

        try (DirectoryStream<Path> taskDirs = Files.newDirectoryStream(dateDir)) {
            for (Path taskDir : taskDirs) {
                if (!Files.isDirectory(taskDir)) {
                    continue;
                }

                // 检查任务目录是否为空
                if (isDirectoryEmpty(taskDir)) {
                    Files.delete(taskDir);
                    deletedCount++;
                    log.info("删除空任务目录: {}", taskDir.toAbsolutePath());
                }
            }
        }

        return deletedCount;
    }

    /**
     * 检查目录是否为空
     * @param dir 目录路径
     * @return 是否为空
     */
    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }

    /**
     * 递归删除目录
     * @param dir 目录路径
     */
    private void deleteDirectory(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除文件失败: {}", path.toAbsolutePath(), e);
                        }
                    });
        } catch (IOException e) {
            log.error("删除目录失败: {}", dir.toAbsolutePath(), e);
        }
    }
}
