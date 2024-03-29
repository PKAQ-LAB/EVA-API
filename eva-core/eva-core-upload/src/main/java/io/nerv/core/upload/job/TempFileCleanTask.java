package io.nerv.core.upload.job;

import io.nerv.core.upload.util.FileProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;


/**
 * 缓存目录文件清理任务
 * 定时清理因用户上传后未提交表单产生的临时文件
 * <p>
 * 举个例子，比如 我有个表单，里面有合同上传，文件是异步上传到文件服务器然后返回文件id的,
 * 然后这个人一直上传 就是不提交表单，那么它上传的这个合同文件是不是没有任何价值
 *
 * @author PKAQ
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TempFileCleanTask implements SchedulingConfigurer {

    private final FileProvider fileUploadProvider;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        /**
         * nginx 清空缓存目录
         * fastdfs 清空当前时间点 1 小时之前的文件
         */
        taskRegistrar.addTriggerTask(() -> {
                    log.info("File Tmp Clear ---- > ");
                    fileUploadProvider.tempClean();
                },
                triggerContext -> new PeriodicTrigger(Duration.ofHours(2)).nextExecution(triggerContext));
    }
}
