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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class PortScanExecutor extends AbstractToolExecutor<PortScanExecutor.PortScanRequest, PortScanExecutor.PortScanResult> {

    private static final int DEFAULT_TIMEOUT = 200;
    private static final int DEFAULT_CONCURRENCY = 50;
    private static final int MAX_PORT = 65535;

    private static final Map<Integer, String> COMMON_PORTS = Map.ofEntries(
            Map.entry(21, "FTP"),
            Map.entry(22, "SSH"),
            Map.entry(23, "Telnet"),
            Map.entry(25, "SMTP"),
            Map.entry(53, "DNS"),
            Map.entry(80, "HTTP"),
            Map.entry(110, "POP3"),
            Map.entry(135, "MSRPC"),
            Map.entry(139, "NetBIOS"),
            Map.entry(143, "IMAP"),
            Map.entry(443, "HTTPS"),
            Map.entry(445, "SMB"),
            Map.entry(993, "IMAPS"),
            Map.entry(995, "POP3S"),
            Map.entry(1433, "SQL Server"),
            Map.entry(3306, "MySQL"),
            Map.entry(3389, "RDP"),
            Map.entry(5432, "PostgreSQL"),
            Map.entry(5900, "VNC"),
            Map.entry(6379, "Redis"),
            Map.entry(8080, "HTTP-Proxy"),
            Map.entry(8443, "HTTPS-Alt"),
            Map.entry(27017, "MongoDB")
    );

    @Override
    public String getToolCode() {
        return "port-scan";
    }

    @Override
    public ToolType getToolType() {
        return ToolType.INSTANT;
    }

    @Override
    public void validate(PortScanRequest request) throws BusinessException {
        validateNotNull(request, "请求");
        validateNotEmpty(request.getHost(), "目标主机");
        if (request.getStartPort() != null) {
            validateRange(request.getStartPort(), "起始端口", 1, MAX_PORT);
        }
        if (request.getEndPort() != null) {
            validateRange(request.getEndPort(), "结束端口", 1, MAX_PORT);
        }
        if (request.getPort() != null) {
            validateRange(request.getPort(), "端口", 1, MAX_PORT);
        }
        if (request.getStartPort() != null && request.getEndPort() != null) {
            if (request.getStartPort() > request.getEndPort()) {
                throw new BusinessException("起始端口不能大于结束端口");
            }
        }
        if (request.getConcurrency() != null) {
            validateRange(request.getConcurrency(), "并发数", 1, 200);
        }
        if (request.getTimeout() != null) {
            validateRange(request.getTimeout(), "超时时间", 10, 10000);
        }
    }

    @Override
    protected PortScanResult doExecute(PortScanRequest request, ToolContext context) throws Exception {
        String host = request.getHost();
        String protocol = request.getProtocol() != null ? request.getProtocol() : "TCP";
        int timeout = request.getTimeout() != null ? request.getTimeout() : DEFAULT_TIMEOUT;
        int concurrency = request.getConcurrency() != null ? request.getConcurrency() : DEFAULT_CONCURRENCY;

        List<Integer> portsToScan = new ArrayList<>();
        if (request.getPort() != null) {
            portsToScan.add(request.getPort());
        } else if (request.getStartPort() != null && request.getEndPort() != null) {
            for (int port = request.getStartPort(); port <= request.getEndPort(); port++) {
                portsToScan.add(port);
            }
        } else {
            portsToScan.addAll(COMMON_PORTS.keySet().stream().sorted().toList());
        }

        log.info("端口扫描开始: host={}, protocol={}, ports={}, timeout={}, concurrency={}", 
                host, protocol, portsToScan.size(), timeout, concurrency);

        List<PortInfo> openPorts = scanPorts(host, portsToScan, protocol, timeout, concurrency);

        PortScanResult result = new PortScanResult();
        result.setSuccess(true);
        result.setHost(host);
        result.setProtocol(protocol);
        result.setScannedPorts(portsToScan.size());
        result.setOpenPorts(openPorts.size());
        result.setPorts(openPorts);

        log.info("端口扫描完成: host={}, openPorts={}", host, openPorts.size());
        return result;
    }

    private List<PortInfo> scanPorts(String host, List<Integer> ports, String protocol, int timeout, int concurrency) {
        List<PortInfo> openPorts = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger progress = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch latch = new CountDownLatch(ports.size());

        try {
            for (int port : ports) {
                executor.submit(() -> {
                    try {
                        boolean isOpen = "UDP".equalsIgnoreCase(protocol) 
                                ? scanUdpPort(host, port, timeout) 
                                : scanTcpPort(host, port, timeout);
                        
                        if (isOpen) {
                            PortInfo portInfo = PortInfo.builder()
                                    .port(port)
                                    .service(COMMON_PORTS.getOrDefault(port, "Unknown"))
                                    .open(true)
                                    .build();
                            openPorts.add(portInfo);
                        }
                        
                        int current = progress.incrementAndGet();
                        if (current % 100 == 0) {
                            log.info("端口扫描进度: {}/{}", current, ports.size());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("端口扫描被中断");
        } finally {
            executor.shutdownNow();
        }

        openPorts.sort(Comparator.comparingInt(PortInfo::getPort));
        return openPorts;
    }

    private boolean scanTcpPort(String host, int port, int timeout) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            
            if (channel.finishConnect()) {
                return true;
            }
            
            long endTime = System.currentTimeMillis() + timeout;
            while (System.currentTimeMillis() < endTime) {
                if (channel.finishConnect()) {
                    return true;
                }
                Thread.sleep(10);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean scanUdpPort(String host, int port, int timeout) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            InetSocketAddress address = new InetSocketAddress(host, port);
            
            byte[] data = new byte[]{0x00, 0x00};
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.send(buffer, address);
            
            ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
            long endTime = System.currentTimeMillis() + timeout;
            
            while (System.currentTimeMillis() < endTime) {
                try {
                    SocketAddress sender = channel.receive(receiveBuffer);
                    if (sender != null) {
                        return true;
                    }
                } catch (IOException e) {
                }
                Thread.sleep(10);
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String buildErrorMessage(Exception e) {
        return "端口扫描失败: " + e.getMessage();
    }

    @Override
    public Map<String, Object> getToolConfig() {
        return Map.of(
                "name", "端口扫描器",
                "description", "扫描目标主机的开放端口，支持TCP和UDP协议",
                "commonPorts", COMMON_PORTS
        );
    }

    @Data
    public static class PortScanRequest {
        private String host;
        private Integer port;
        private Integer startPort;
        private Integer endPort;
        private String protocol;
        private Integer timeout;
        private Integer concurrency;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class PortScanResult extends BaseToolResponse {
        private String host;
        private String protocol;
        private Integer scannedPorts;
        private Integer openPorts;
        private List<PortInfo> ports;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class PortInfo {
        private Integer port;
        private String service;
        private Boolean open;
    }
}
