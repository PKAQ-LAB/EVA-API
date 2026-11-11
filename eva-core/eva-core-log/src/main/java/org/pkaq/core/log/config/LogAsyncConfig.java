package org.pkaq.core.log.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.pkaq.core.tenant.TenantLogContext;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 日志异步线程池配置
 *
 * @author PKAQ
 * @date 2026-03-10
 */
@Configuration
@EnableAsync
public class LogAsyncConfig {

    /**
     * 日志异步保存线程池
     */
    @Bean("log_task")
    public Executor logTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(256);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("log-task-");
        executor.setTaskDecorator(task -> {
            Map<String, String> captured = MDC.getCopyOfContextMap();
            Map<String, String> snapshot = captured == null ? new HashMap<>() : new HashMap<>(captured);
            String tenantId = TenantLogContext.getTenantId();
            // 提交时固定日志归属，不跨线程传播认证身份或数据库路由。
            if (tenantId != null) {
                snapshot.put(TenantLogContext.KEY, tenantId);
            }
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                try {
                    MDC.setContextMap(snapshot);
                    task.run();
                } finally {
                    // 线程复用和 CallerRunsPolicy 均恢复原 MDC。
                    if (previous == null) {
                        MDC.clear();
                    } else {
                        MDC.setContextMap(previous);
                    }
                }
            };
        });
        // 调用者线程执行，保证日志不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
