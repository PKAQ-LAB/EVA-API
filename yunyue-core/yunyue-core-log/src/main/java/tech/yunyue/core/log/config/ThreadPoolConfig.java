package tech.yunyue.core.log.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class ThreadPoolConfig {
    /**
     * 日志线程池任务执行器
     * @return
     */
    @Bean(name = "log_task")
    public Executor logTaskExecutor() {
        // 创建线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 设置核心池大小
        executor.setMaxPoolSize(10); // 设置最大池大小，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setQueueCapacity(100); // 设置队列容量
        executor.setKeepAliveSeconds(60); // 设置保持活动秒数，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        executor.setThreadNamePrefix("BizLog-Thread-"); // 设置线程名称前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 设置拒绝的执行处理策略
        return executor;
    }

    /**
     * 文件线程池任务执行器
     * @return
     */
    @Bean(name = "file_task")
    public Executor fileTaskExecutor() {
        // 创建线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 设置核心池大小
        executor.setMaxPoolSize(10); // 设置最大池大小，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setQueueCapacity(100); // 设置队列容量
        executor.setKeepAliveSeconds(60); // 设置保持活动秒数，当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        executor.setThreadNamePrefix("BizLog-Thread-"); // 设置线程名称前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 设置拒绝的执行处理策略
        return executor;
    }
}
