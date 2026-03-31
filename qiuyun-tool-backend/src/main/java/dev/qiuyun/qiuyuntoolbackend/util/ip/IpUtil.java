package dev.qiuyun.qiuyuntoolbackend.util.ip;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * IP工具类
 * 提供获取客户端真实IP地址的功能
 */
@Slf4j
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IPV6_SHORT = "::1";

    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Cluster-Client-IP",
            "Forwarded-For",
            "Forwarded"
    );

    private IpUtil() {
    }

    /**
     * 获取当前请求的客户端IP地址
     * 从RequestContextHolder中获取当前请求
     *
     * @return 客户端IP地址，获取失败返回空字符串
     */
    public static String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return getClientIp(request);
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "";
    }

    /**
     * 从HttpServletRequest中获取客户端真实IP地址
     *
     * 支持多种代理服务器的IP获取方式，按优先级检查：
     * - X-Forwarded-For
     * - X-Real-IP
     * - Proxy-Client-IP
     * - WL-Proxy-Client-IP
     * - HTTP_CLIENT_IP
     * - HTTP_X_FORWARDED_FOR
     * - X-Cluster-Client-IP
     * - Forwarded-For
     * - Forwarded
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址，获取失败返回request.getRemoteAddr()
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String ip = null;

        for (String header : IP_HEADERS) {
            ip = request.getHeader(header);
            if (!isInvalidIp(ip)) {
                break;
            }
        }

        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null) {
            ip = ip.trim();

            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            if (LOCALHOST_IPV6.equals(ip) || LOCALHOST_IPV6_SHORT.equals(ip)) {
                ip = LOCALHOST_IPV4;
            }
        }

        return ip;
    }

    /**
     * 检查IP是否无效
     *
     * @param ip IP地址
     * @return true表示无效，false表示有效
     */
    private static boolean isInvalidIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return true;
        }
        String trimmedIp = ip.trim();
        return UNKNOWN.equalsIgnoreCase(trimmedIp);
    }

    /**
     * 判断是否是内网IP或本地IP
     *
     * @param ip IP地址
     * @return true表示是内网/本地IP，false表示是公网IP
     */
    public static boolean isInternalOrLocalIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        ip = ip.trim();

        if ("127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return true;
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return true;
        }

        if (ip.startsWith("10.")) {
            return true;
        }

        if (ip.startsWith("172.")) {
            try {
                int secondOctet = Integer.parseInt(ip.split("\\.")[1]);
                if (secondOctet >= 16 && secondOctet <= 31) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (ip.startsWith("192.168.")) {
            return true;
        }

        return false;
    }
}
