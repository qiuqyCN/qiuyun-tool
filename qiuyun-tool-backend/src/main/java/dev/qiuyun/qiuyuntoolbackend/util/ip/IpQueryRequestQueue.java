package dev.qiuyun.qiuyuntoolbackend.util.ip;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * IP查询请求队列组件
 * 
 * 功能特性：
 * - 使用Guava RateLimiter实现令牌桶限流，确保每秒不超过1次请求
 * - 使用阻塞队列缓冲请求，避免大量并发请求直接压垮API
 * - 单消费者模式处理队列，保证请求顺序
 * - 优先检查本地缓存，命中则直接返回，无需排队
 * - 支持请求超时机制，默认30秒超时
 * - 优雅的启动和关闭机制
 * 
 * @author qiuyun
 */
@Slf4j
@Component
public class IpQueryRequestQueue {

    private static final double PERMITS_PER_SECOND = 1.0;
    private static final int QUEUE_CAPACITY = 1000;
    private static final long REQUEST_TIMEOUT_SECONDS = 30;

    @Autowired
    private TaobaoIpApiClient taobaoIpApiClient;

    private final BlockingQueue<IpQueryTask> requestQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final RateLimiter rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);
    private ExecutorService consumerExecutor;
    private volatile boolean running = true;

    /**
     * IP查询任务
     */
    @Data
    @AllArgsConstructor
    public static class IpQueryTask {
        private String ip;
        private CompletableFuture<TaobaoIpApiClient.CachedIpResponse> future;
    }

    /**
     * 初始化队列处理器
     * 创建单线程消费者，启动队列处理
     */
    @PostConstruct
    public void init() {
        consumerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ip-query-consumer");
            t.setDaemon(true);
            return t;
        });
        consumerExecutor.submit(this::processQueue);
        log.info("IP查询队列初始化成功，限流: {} 请求/秒", PERMITS_PER_SECOND);
    }

    /**
     * 关闭队列处理器
     * 优雅地停止消费者线程
     */
    @PreDestroy
    public void shutdown() {
        running = false;
        if (consumerExecutor != null) {
            consumerExecutor.shutdown();
            try {
                if (!consumerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    consumerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                consumerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("IP查询队列已关闭");
    }

    /**
     * 提交IP查询请求
     * 
     * 流程：
     * 1. 先尝试从本地缓存获取，命中则立即返回
     * 2. 缓存未命中则将请求加入队列
     * 3. 返回CompletableFuture，异步等待结果
     * 
     * @param ip 要查询的IP地址
     * @return 查询结果的CompletableFuture
     */
    public CompletableFuture<TaobaoIpApiClient.CachedIpResponse> submit(String ip) {
        CompletableFuture<TaobaoIpApiClient.CachedIpResponse> future = new CompletableFuture<>();
        
        TaobaoIpApiClient.CachedIpResponse cachedResult = taobaoIpApiClient.queryFromLocalCache(ip);
        if (cachedResult != null) {
            future.complete(cachedResult);
            return future;
        }

        try {
            IpQueryTask task = new IpQueryTask(ip, future);
            boolean offered = requestQueue.offer(task, 1, TimeUnit.SECONDS);
            if (!offered) {
                future.completeExceptionally(new RuntimeException("队列已满，请稍后重试"));
            }
        } catch (InterruptedException e) {
            future.completeExceptionally(new RuntimeException("请求被中断"));
            Thread.currentThread().interrupt();
        }

        return future.orTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 队列处理主循环
     * 持续从队列中获取任务并处理，使用take()阻塞等待避免CPU空转
     */
    private void processQueue() {
        while (running) {
            try {
                IpQueryTask task = requestQueue.take();
                processTask(task);
            } catch (InterruptedException e) {
                if (running) {
                    log.warn("队列处理线程被中断", e);
                }
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("队列处理异常", e);
            }
        }
    }

    /**
     * 处理单个IP查询任务
     * 
     * 流程：
     * 1. 获取令牌，确保不超过限流
     * 2. 调用API客户端查询IP
     * 3. 完成future，通知调用方
     * 
     * @param task 要处理的查询任务
     */
    private void processTask(IpQueryTask task) {
        try {
            rateLimiter.acquire();
            TaobaoIpApiClient.CachedIpResponse response = taobaoIpApiClient.queryIp(task.getIp());
            task.getFuture().complete(response);
        } catch (Exception e) {
            log.error("处理IP查询失败: {}", task.getIp(), e);
            task.getFuture().completeExceptionally(e);
        }
    }
}
