package dev.qiuyun.qiuyuntoolbackend.executor;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutorRegistry {

    private final List<ToolExecutor<?, ?>> executors;

    private final Map<String, ToolExecutor<?, ?>> executorMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (ToolExecutor<?, ?> executor : executors) {
            String toolCode = executor.getToolCode();
            executorMap.put(toolCode, executor);
            log.info("Registered tool executor: {} -> {}", toolCode, executor.getClass().getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    public <REQ, RES> ToolExecutor<REQ, RES> getExecutor(String toolCode) {
        ToolExecutor<?, ?> executor = executorMap.get(toolCode);
        if (executor == null) {
            return null;
        }
        return (ToolExecutor<REQ, RES>) executor;
    }

    public boolean hasExecutor(String toolCode) {
        return executorMap.containsKey(toolCode);
    }

    public ToolType getToolType(String toolCode) {
        ToolExecutor<?, ?> executor = executorMap.get(toolCode);
        return executor != null ? executor.getToolType() : null;
    }

    public Map<String, ToolType> getAllToolTypes() {
        Map<String, ToolType> types = new ConcurrentHashMap<>();
        executorMap.forEach((code, executor) -> types.put(code, executor.getToolType()));
        return types;
    }
}
