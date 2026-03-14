package org.pkaq.core.log.config;

import org.pkaq.core.log.condition.BizlogSupporterCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 日志异步线程池配置
 * 仅在日志功能启用时生效
 *
 * @author PKAQ
 * @date 2026-03-10
 */
@Configuration
@EnableAsync
@Conditional(BizlogSupporterCondition.class)
public class LogAsyncConfig {

    /**
     * 日志异步任务线程池
     *
     * @return 线程池执行器
     */
    @Bean("log_task")
    public Executor logTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(256);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("log-task-");
        // 队列满时由调用线程执行，确保日志不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
