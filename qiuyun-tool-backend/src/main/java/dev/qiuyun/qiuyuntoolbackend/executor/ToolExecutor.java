package dev.qiuyun.qiuyuntoolbackend.executor;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;

import java.util.Map;

public interface ToolExecutor<REQ, RES> {

    String getToolCode();

    ToolType getToolType();

    void validate(REQ request) throws BusinessException;

    RES execute(REQ request, ToolContext context) throws BusinessException;

    default Map<String, Object> getToolConfig() {
        return Map.of();
    }
}
