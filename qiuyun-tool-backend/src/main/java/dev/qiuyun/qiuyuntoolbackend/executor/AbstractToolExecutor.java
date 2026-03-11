package dev.qiuyun.qiuyuntoolbackend.executor;

import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 抽象工具执行器基类
 * 提供通用的验证和异常处理逻辑
 *
 * @param <REQ> 请求类型
 * @param <RES> 响应类型，必须继承 BaseToolResponse
 */
@Slf4j
public abstract class AbstractToolExecutor<REQ, RES extends BaseToolResponse> implements ToolExecutor<REQ, RES> {

    @Override
    public final RES execute(REQ request, ToolContext context) throws BusinessException {
        try {
            validate(request);
            return doExecute(request, context);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("工具执行错误 [{}]: {}", getToolCode(), e.getMessage(), e);
            throw new BusinessException(buildErrorMessage(e));
        }
    }

    /**
     * 执行具体的工具逻辑
     * 子类必须实现此方法
     *
     * @param request 请求参数
     * @param context 工具上下文
     * @return 响应结果
     * @throws Exception 执行异常
     */
    protected abstract RES doExecute(REQ request, ToolContext context) throws Exception;

    /**
     * 构建错误消息
     * 子类可以覆盖此方法自定义错误消息
     *
     * @param e 异常
     * @return 错误消息
     */
    protected String buildErrorMessage(Exception e) {
        return "操作失败: " + e.getMessage();
    }

    // ==================== 通用验证方法 ====================

    /**
     * 验证对象不为 null
     *
     * @param value     要验证的对象
     * @param fieldName 字段名称
     * @throws BusinessException 如果对象为 null
     */
    protected void validateNotNull(Object value, String fieldName) throws BusinessException {
        if (value == null) {
            throw new BusinessException(fieldName + "不能为空");
        }
    }

    /**
     * 验证字符串不为空（不为 null 且不为空字符串）
     *
     * @param value     要验证的字符串
     * @param fieldName 字段名称
     * @throws BusinessException 如果字符串为空
     */
    protected void validateNotEmpty(String value, String fieldName) throws BusinessException {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(fieldName + "不能为空");
        }
    }

    /**
     * 验证枚举值是否合法
     *
     * @param value       要验证的值
     * @param fieldName   字段名称
     * @param validValues 合法的值集合
     * @throws BusinessException 如果值不合法
     */
    protected void validateEnum(String value, String fieldName, Collection<String> validValues) throws BusinessException {
        if (!validValues.contains(value)) {
            throw new BusinessException(fieldName + "必须是以下值之一: " + String.join(", ", validValues));
        }
    }

    /**
     * 验证数值范围
     *
     * @param value     要验证的数值
     * @param fieldName 字段名称
     * @param min       最小值
     * @param max       最大值
     * @throws BusinessException 如果数值不在范围内
     */
    protected void validateRange(int value, String fieldName, int min, int max) throws BusinessException {
        if (value < min || value > max) {
            throw new BusinessException(fieldName + "必须在 " + min + " 到 " + max + " 之间");
        }
    }

    /**
     * 验证数值范围（长整型）
     *
     * @param value     要验证的数值
     * @param fieldName 字段名称
     * @param min       最小值
     * @param max       最大值
     * @throws BusinessException 如果数值不在范围内
     */
    protected void validateRange(long value, String fieldName, long min, long max) throws BusinessException {
        if (value < min || value > max) {
            throw new BusinessException(fieldName + "必须在 " + min + " 到 " + max + " 之间");
        }
    }
}
