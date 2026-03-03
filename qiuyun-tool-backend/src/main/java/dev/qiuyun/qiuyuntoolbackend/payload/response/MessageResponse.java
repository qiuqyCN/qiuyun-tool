package dev.qiuyun.qiuyuntoolbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;

    /**
     * 创建成功响应
     */
    public static MessageResponse success(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }
}
