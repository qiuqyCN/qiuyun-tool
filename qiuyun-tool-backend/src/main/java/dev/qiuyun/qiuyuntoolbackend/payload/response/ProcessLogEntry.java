package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处理日志条目
 * 用于记录任务执行过程中的步骤日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessLogEntry {

    /**
     * 日志内容
     */
    private String message;

    /**
     * 日志类型：INFO, WARN, ERROR, SUCCESS
     */
    private String type;

    /**
     * 时间戳
     */
    private LocalDateTime time;
}
