package dev.qiuyun.qiuyuntoolbackend.executor.network;

import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.util.ip.CachedIpResponse;
import dev.qiuyun.qiuyuntoolbackend.util.ip.PconlineIpApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PconlineIpQueryExecutor extends AbstractIpQueryExecutor<AbstractIpQueryExecutor.IpQueryResult> {

    @Autowired
    private PconlineIpApiClient pconlineIpApiClient;

    @Override
    public String getToolCode() {
        return "pconline-ip-query";
    }

    @Override
    protected IpQueryResult doExecute(IpQueryRequest request, ToolContext context) throws Exception {
        String ip = prepareAndValidateIp(request.getIp());
        log.info("太平洋在线IP查询请求: ip={}", ip);

        CachedIpResponse apiResponse = pconlineIpApiClient.queryIp(ip);

        IpQueryResult result = new IpQueryResult();
        populateResult(result, apiResponse);

        return result;
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "太平洋在线IP查询",
                "description", "使用太平洋在线API查询IP地址的地理位置信息"
        );
    }
}
