package dev.qiuyun.qiuyuntoolbackend.executor.network;

import dev.qiuyun.qiuyuntoolbackend.enums.ToolType;
import dev.qiuyun.qiuyuntoolbackend.exception.BusinessException;
import dev.qiuyun.qiuyuntoolbackend.executor.AbstractToolExecutor;
import dev.qiuyun.qiuyuntoolbackend.executor.ToolContext;
import dev.qiuyun.qiuyuntoolbackend.executor.common.BaseToolResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Record;

import java.util.*;

@Slf4j
@Component
public class DnsQueryExecutor extends AbstractToolExecutor<DnsQueryExecutor.DnsQueryRequest, DnsQueryExecutor.DnsQueryResult> {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "A", "AAAA", "CNAME", "MX", "NS", "SOA", "TXT", "PTR", "SRV"
    );

    @Override
    public String getToolCode() {
        return "dns-query";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(DnsQueryRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getDomain(), "域名");
        if (request.getType() != null) {
            validateEnum(request.getType().toUpperCase(), "记录类型", SUPPORTED_TYPES);
        }
    }

    @Override
    protected DnsQueryResult doExecute(DnsQueryRequest request, ToolContext context) throws Exception {
        String domain = request.getDomain();
        String type = request.getType() != null ? request.getType().toUpperCase() : "A";
        String dnsServer = request.getDnsServer();

        log.info("DNS查询开始: domain={}, type={}, dnsServer={}", domain, type, dnsServer);

        List<DnsRecord> records = queryDns(domain, type, dnsServer);

        DnsQueryResult result = new DnsQueryResult();
        result.setSuccess(true);
        result.setDomain(domain);
        result.setType(type);
        result.setRecords(records);

        log.info("DNS查询完成: domain={}, records={}", domain, records.size());
        return result;
    }

    private List<DnsRecord> queryDns(String domain, String type, String dnsServer) throws Exception {
        List<DnsRecord> records = new ArrayList<>();

        Resolver resolver;
        if (dnsServer != null && !dnsServer.trim().isEmpty()) {
            resolver = new SimpleResolver(dnsServer);
        } else {
            resolver = new SimpleResolver();
        }

        int recordType = Type.value(type);
        Name name = Name.fromString(domain.endsWith(".") ? domain : domain + ".");

        Record queryRecord = Record.newRecord(name, recordType, DClass.IN);
        Message query = Message.newQuery(queryRecord);

        Message response = resolver.send(query);

        Record[] answerRecords = response.getSectionArray(Section.ANSWER);
        for (Record record : answerRecords) {
            DnsRecord dnsRecord = parseRecord(record);
            if (dnsRecord != null) {
                records.add(dnsRecord);
            }
        }

        return records;
    }

    private DnsRecord parseRecord(Record record) {
        DnsRecord.DnsRecordBuilder builder = DnsRecord.builder()
                .name(record.getName().toString())
                .type(Type.string(record.getType()))
                .ttl(record.getTTL());

        if (record instanceof ARecord) {
            ARecord aRecord = (ARecord) record;
            builder.value(aRecord.getAddress().getHostAddress());
        } else if (record instanceof AAAARecord) {
            AAAARecord aaaaRecord = (AAAARecord) record;
            builder.value(aaaaRecord.getAddress().getHostAddress());
        } else if (record instanceof CNAMERecord) {
            CNAMERecord cnameRecord = (CNAMERecord) record;
            builder.value(cnameRecord.getTarget().toString());
        } else if (record instanceof MXRecord) {
            MXRecord mxRecord = (MXRecord) record;
            builder.value(mxRecord.getTarget().toString());
            builder.priority(mxRecord.getPriority());
        } else if (record instanceof NSRecord) {
            NSRecord nsRecord = (NSRecord) record;
            builder.value(nsRecord.getTarget().toString());
        } else if (record instanceof SOARecord) {
            SOARecord soaRecord = (SOARecord) record;
            builder.value(soaRecord.getHost() + " " + soaRecord.getAdmin());
            Map<String, Object> soaDetails = new HashMap<>();
            soaDetails.put("host", soaRecord.getHost().toString());
            soaDetails.put("admin", soaRecord.getAdmin().toString());
            soaDetails.put("serial", soaRecord.getSerial());
            soaDetails.put("refresh", soaRecord.getRefresh());
            soaDetails.put("retry", soaRecord.getRetry());
            soaDetails.put("expire", soaRecord.getExpire());
            soaDetails.put("minimum", soaRecord.getMinimum());
            builder.details(soaDetails);
        } else if (record instanceof TXTRecord) {
            TXTRecord txtRecord = (TXTRecord) record;
            List<String> strings = txtRecord.getStrings();
            builder.value(String.join(" ", strings));
        } else if (record instanceof PTRRecord) {
            PTRRecord ptrRecord = (PTRRecord) record;
            builder.value(ptrRecord.getTarget().toString());
        } else if (record instanceof SRVRecord) {
            SRVRecord srvRecord = (SRVRecord) record;
            builder.value(srvRecord.getTarget().toString() + ":" + srvRecord.getPort());
            builder.priority(srvRecord.getPriority());
            builder.weight(srvRecord.getWeight());
            builder.port(srvRecord.getPort());
        } else {
            builder.value(record.toString());
        }

        return builder.build();
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "DNS查询失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "DNS查询",
                "description", "查询域名的DNS记录，支持多种记录类型",
                "supportedTypes", SUPPORTED_TYPES.stream().sorted().toList()
        );
    }

    @Data
    public static class DnsQueryRequest {
        private String domain;
        private String type;
        private String dnsServer;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DnsQueryResult extends BaseToolResponse {
        private String domain;
        private String type;
        private List<DnsRecord> records;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class DnsRecord {
        private String name;
        private String type;
        private Long ttl;
        private String value;
        private Integer priority;
        private Integer weight;
        private Integer port;
        private Map<String, Object> details;
    }
}
