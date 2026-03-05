package dev.qiuyun.qiuyuntoolbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class ToolConfig {

    @Value("${tool.temp.dir:${java.io.tmpdir}/qiuyun-tools}")
    private String tempDir;

    @Value("${tool.file.max-size:209715200}")
    private long maxFileSize;

    @Value("${tool.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    public String getTempDir() {
        return tempDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public boolean isCleanupEnabled() {
        return cleanupEnabled;
    }

    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("tool-executor-");
        executor.initialize();
        return executor;
    }
}
