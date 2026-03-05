package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolExecuteResponse<T> {

    private String taskId;

    private String status;

    private T result;

    private String downloadUrl;

    private Integer progress;

    private String message;
}
