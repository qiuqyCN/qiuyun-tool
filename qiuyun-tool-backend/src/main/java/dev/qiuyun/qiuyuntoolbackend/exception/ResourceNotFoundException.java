package dev.qiuyun.qiuyuntoolbackend.exception;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(ErrorCode.NOT_FOUND, String.format("%s 不存在: %s", resourceName, identifier));
    }
}
