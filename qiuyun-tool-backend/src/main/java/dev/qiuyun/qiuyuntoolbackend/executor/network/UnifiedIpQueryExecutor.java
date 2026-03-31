package dev.qiuyun.qiuyuntoolbackend.executor.network;

import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.util.ip.CachedIpResponse;
import dev.qiuyun.qiuyuntoolbackend.util.ip.PconlineIpApiClient;
import dev.qiuyun.qiuyuntoolbackend.util.ip.TaobaoIpApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class UnifiedIpQueryExecutor extends AbstractIpQueryExecutor<AbstractIpQueryExecutor.IpQueryResult> {

    @Autowired
    private PconlineIpApiClient pconlineIpApiClient;

    @Autowired
    private TaobaoIpApiClient taobaoIpApiClient;

    @Override
    public String getToolCode() {
        return "ip-query";
    }

    @Override
    protected IpQueryResult doExecute(IpQueryRequest request, ToolContext context) throws Exception {
        String ip = prepareAndValidateIp(request.getIp());
        log.info("IP查询请求: ip={}", ip);

        CachedIpResponse apiResponse;
        String primarySource = null;
        String fallbackSource = null;

        try {
            log.debug("尝试使用太平洋在线API查询");
            apiResponse = pconlineIpApiClient.queryIp(ip);
            primarySource = "pconline";
            log.info("太平洋在线API查询成功: ip={}", ip);
        } catch (Exception e) {
            log.warn("太平洋在线API查询失败，尝试使用淘宝API兜底: ip={}, error={}", ip, e.getMessage());
            try {
                apiResponse = taobaoIpApiClient.queryIp(ip);
                fallbackSource = "taobao";
                log.info("淘宝API查询成功(兜底): ip={}", ip);
            } catch (Exception ex) {
                log.error("两个API都查询失败: ip={}", ip, ex);
                throw new BusinessException("IP查询失败，请稍后重试");
            }
        }

        IpQueryResult result = new IpQueryResult();
        populateResult(result, apiResponse);

        if (primarySource != null) {
            result.setSource(primarySource + "/" + result.getSource());
        } else {
            result.setSource(fallbackSource + "/" + result.getSource());
        }

        return result;
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "IP地址查询",
                "description", "查询IP地址的地理位置信息，包括国家、省份、城市和运营商（支持双API兜底）"
        );
    }
}
