package dev.qiuyun.qiuyuntoolbackend.executor.dev;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * User-Agent 解析工具执行器
 * 使用 Yauaa 库解析浏览器、操作系统、设备等信息
 */
@Slf4j
@Component
public class UserAgentParserExecutor extends AbstractToolExecutor<UserAgentParserExecutor.UserAgentRequest, UserAgentParserExecutor.UserAgentInfo> {

    private UserAgentAnalyzer uaa;

    @PostConstruct
    public void init() {
        // 禁用 Yauaa 版本信息打印
        System.setProperty("yauaa.version.log", "false");

        uaa = UserAgentAnalyzer.newBuilder()
                .withField(UserAgent.AGENT_NAME)
                .withField(UserAgent.AGENT_VERSION)
                .withField(UserAgent.OPERATING_SYSTEM_NAME)
                .withField(UserAgent.OPERATING_SYSTEM_VERSION)
                .withField(UserAgent.DEVICE_NAME)
                .withField(UserAgent.DEVICE_BRAND)
                .withField(UserAgent.DEVICE_CLASS)
                .withField(UserAgent.LAYOUT_ENGINE_NAME)
                .withField(UserAgent.LAYOUT_ENGINE_VERSION)
                .build();
        log.info("UserAgentAnalyzer initialized successfully");
    }

    @Override
    public String getToolCode() {
        return "user-agent-parser";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(UserAgentRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getUserAgent(), "User-Agent");
    }

    @Override
    protected UserAgentInfo doExecute(UserAgentRequest request, ToolContext context) throws Exception {
        String uaString = request.getUserAgent().trim();
        log.debug("Parsing User-Agent: {}", uaString);

        // 使用 Yauaa 解析
        UserAgent agent = uaa.parse(uaString);

        UserAgentInfo info = new UserAgentInfo();
        info.setOriginal(uaString);

        // 浏览器信息
        info.setBrowser(getValueOrDefault(agent, UserAgent.AGENT_NAME));
        info.setBrowserVersion(getValueOrDefault(agent, UserAgent.AGENT_VERSION));

        // 操作系统信息
        String osName = getValueOrDefault(agent, UserAgent.OPERATING_SYSTEM_NAME);
        String osVersion = getValueOrDefault(agent, UserAgent.OPERATING_SYSTEM_VERSION);

        // 对于 Windows，组合成更友好的显示
        if ("Windows NT".equals(osName) && !"Unknown".equals(osVersion)) {
            // Windows 11 的 Chrome 仍然使用 Windows NT 10.0，无法通过 User-Agent 区分
            if ("10.0".equals(osVersion)) {
                info.setOs("Windows 10/11");
                info.setOsVersion("(无法通过 User-Agent 区分)");
            } else {
                info.setOs("Windows " + osVersion);
                info.setOsVersion("");
            }
        } else if ("Windows NT".equals(osName)) {
            // 尝试从 User-Agent 中提取 Windows 版本
            if (uaString.contains("Windows NT 10.0")) {
                info.setOs("Windows 10/11");
                info.setOsVersion("(无法通过 User-Agent 区分)");
            } else if (uaString.contains("Windows NT 6.3")) {
                info.setOs("Windows 8.1");
            } else if (uaString.contains("Windows NT 6.2")) {
                info.setOs("Windows 8");
            } else if (uaString.contains("Windows NT 6.1")) {
                info.setOs("Windows 7");
            } else {
                info.setOs("Windows");
            }
        } else {
            info.setOs(osName);
            info.setOsVersion(osVersion);
        }

        // 设备信息
        info.setDevice(getValueOrDefault(agent, UserAgent.DEVICE_NAME));
        info.setBrand(getValueOrDefault(agent, UserAgent.DEVICE_BRAND));
        info.setDeviceType(getValueOrDefault(agent, UserAgent.DEVICE_CLASS));

        // 引擎信息
        info.setEngine(getValueOrDefault(agent, UserAgent.LAYOUT_ENGINE_NAME));
        info.setEngineVersion(getValueOrDefault(agent, UserAgent.LAYOUT_ENGINE_VERSION));

        log.debug("Parsed result: browser={}, os={}, device={}",
                info.getBrowser(), info.getOs(), info.getDevice());

        return info;
    }

    /**
     * 获取字段值，如果为空、Unknown 或 ?? 则返回默认值
     */
    private String getValueOrDefault(UserAgent agent, String fieldName) {
        String value = agent.getValue(fieldName);
        if (value == null || value.isEmpty() || "Unknown".equals(value) || "??".equals(value)) {
            return "Unknown";
        }
        return value;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "解析失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "User-Agent 解析",
                "description", "解析 User-Agent 字符串，获取浏览器、操作系统、设备等信息"
        );
    }

    /**
     * 请求参数
     */
    @Data
    public static class UserAgentRequest {
        /**
         * User-Agent 字符串
         */
        private String userAgent;
    }

    /**
     * User-Agent 解析信息
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class UserAgentInfo extends BaseToolResponse {
        /**
         * 原始 User-Agent 字符串
         */
        private String original;

        /**
         * 浏览器名称
         */
        private String browser;

        /**
         * 浏览器版本
         */
        private String browserVersion;

        /**
         * 操作系统
         */
        private String os;

        /**
         * 操作系统版本
         */
        private String osVersion;

        /**
         * 设备名称
         */
        private String device;

        /**
         * 设备类型 (Desktop/Mobile/Tablet/TV)
         */
        private String deviceType;

        /**
         * 品牌
         */
        private String brand;

        /**
         * 型号
         */
        private String model;

        /**
         * 渲染引擎
         */
        private String engine;

        /**
         * 渲染引擎版本
         */
        private String engineVersion;
    }
}
