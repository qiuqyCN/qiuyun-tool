package dev.qiuyun.qiuyuntoolbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolExecuteRequest<T> {

    @NotBlank(message = "工具ID不能为空")
    private String toolCode;

    private String fileId;

    private T params;
}
