package dev.qiuyun.qiuyuntoolbackend.executor.network;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.whois.WhoisClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WhoisQueryExecutor extends AbstractToolExecutor<WhoisQueryExecutor.WhoisQueryRequest, WhoisQueryExecutor.WhoisQueryResult> {

    private static final String DEFAULT_WHOIS_SERVER = "whois.iana.org";
    private static final int WHOIS_PORT = 43;
    private static final int TIMEOUT = 30000;

    private static final Map<String, String> TLD_WHOIS_SERVERS = Map.ofEntries(
            Map.entry("com", "whois.verisign-grs.com"),
            Map.entry("net", "whois.verisign-grs.com"),
            Map.entry("org", "whois.pir.org"),
            Map.entry("io", "whois.nic.io"),
            Map.entry("cn", "whois.cnnic.cn"),
            Map.entry("top", "whois.nic.top"),
            Map.entry("xyz", "whois.nic.xyz"),
            Map.entry("tech", "whois.nic.tech"),
            Map.entry("online", "whois.nic.online"),
            Map.entry("site", "whois.nic.site"),
            Map.entry("store", "whois.nic.store"),
            Map.entry("fun", "whois.nic.fun"),
            Map.entry("app", "whois.nic.app"),
            Map.entry("dev", "whois.nic.dev"),
            Map.entry("info", "whois.afilias.net"),
            Map.entry("biz", "whois.biz"),
            Map.entry("name", "whois.nic.name"),
            Map.entry("mobi", "whois.dotmobiregistry.net"),
            Map.entry("tv", "whois.nic.tv"),
            Map.entry("cc", "whois.nic.cc"),
            Map.entry("me", "whois.nic.me"),
            Map.entry("co", "whois.nic.co"),
            Map.entry("us", "whois.nic.us"),
            Map.entry("uk", "whois.nic.uk"),
            Map.entry("jp", "whois.jprs.jp"),
            Map.entry("kr", "whois.kisa.or.kr"),
            Map.entry("de", "whois.denic.de"),
            Map.entry("fr", "whois.nic.fr"),
            Map.entry("au", "whois.auda.org.au"),
            Map.entry("ca", "whois.cira.ca"),
            Map.entry("br", "whois.registro.br")
    );

    @Override
    public String getToolCode() {
        return "whois-query";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(WhoisQueryRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getQuery(), "查询内容");
    }

    @Override
    protected WhoisQueryResult doExecute(WhoisQueryRequest request, ToolContext context) throws Exception {
        String query = request.getQuery().trim();
        String server = request.getWhoisServer();

        log.info("Whois查询开始: query={}, server={}", query, server);

        String whoisServer = server != null ? server : determineWhoisServer(query);
        String rawResult = queryWhois(query, whoisServer);

        WhoisQueryResult result = new WhoisQueryResult();
        result.setSuccess(true);
        result.setQuery(query);
        result.setWhoisServer(whoisServer);
        result.setRawResult(rawResult);
        result.setParsedResult(parseWhoisResult(rawResult));

        log.info("Whois查询完成: query={}", query);
        return result;
    }

    private String determineWhoisServer(String query) {
        if (isIpAddress(query)) {
            return "whois.arin.net";
        }

        String tld = extractTld(query);
        if (tld != null && TLD_WHOIS_SERVERS.containsKey(tld.toLowerCase())) {
            return TLD_WHOIS_SERVERS.get(tld.toLowerCase());
        }

        return DEFAULT_WHOIS_SERVER;
    }

    private boolean isIpAddress(String query) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return query.matches(ipPattern);
    }

    private String extractTld(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot > 0 && lastDot < domain.length() - 1) {
            return domain.substring(lastDot + 1);
        }
        return null;
    }

    private String queryWhois(String query, String server) throws Exception {
        WhoisClient whois = new WhoisClient();
        whois.setConnectTimeout(TIMEOUT);
        whois.setDefaultTimeout(TIMEOUT);

        try {
            whois.connect(server, WHOIS_PORT);
            String result = whois.query(query);
            
            String referServer = extractReferralServer(result);
            if (referServer != null && !referServer.equalsIgnoreCase(server)) {
                log.debug("发现Referral Whois服务器: {}", referServer);
                whois.disconnect();
                return queryWhois(query, referServer);
            }
            
            return result;
        } finally {
            if (whois.isConnected()) {
                whois.disconnect();
            }
        }
    }

    private String extractReferralServer(String whoisResult) {
        Pattern pattern = Pattern.compile("ReferralServer:\\s*whois://([^\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(whoisResult);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        pattern = Pattern.compile("Whois Server:\\s*([^\\s]+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(whoisResult);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    private Map<String, String> parseWhoisResult(String rawResult) {
        Map<String, String> parsed = new java.util.LinkedHashMap<>();
        
        String[] lines = rawResult.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("%") || line.startsWith(">>>") || line.startsWith(";")) {
                continue;
            }
            
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                
                if (!key.isEmpty() && !value.isEmpty()) {
                    parsed.put(key, value);
                }
            }
        }
        
        return parsed;
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "Whois查询失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "Whois查询",
                "description", "查询域名或IP地址的Whois信息",
                "defaultWhoisServer", DEFAULT_WHOIS_SERVER,
                "tldWhoisServers", TLD_WHOIS_SERVERS
        );
    }

    @Data
    public static class WhoisQueryRequest {
        private String query;
        private String whoisServer;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WhoisQueryResult extends BaseToolResponse {
        private String query;
        private String whoisServer;
        private String rawResult;
        private Map<String, String> parsedResult;
    }
}
