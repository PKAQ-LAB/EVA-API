package io.nerv.core.upload.job;

import io.nerv.core.upload.util.FileUploadProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.concurrent.TimeUnit;

/**
 * 缓存目录文件清理任务
 * 定时清理因用户上传后未提交表单产生的临时文件
 *
 * 举个例子，比如 我有个表单，里面有合同上传，文件是异步上传到文件服务器然后返回文件id的,
 * 然后这个人一直上传 就是不提交表单，那么它上传的这个合同文件是不是没有任何价值
 */
@Configuration
@EnableScheduling
public class TempFileCleanTask implements SchedulingConfigurer {

    @Autowired
    private FileUploadProvider fileUploadProvider;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        /**
         * nginx 清空缓存目录
         * fastdfs 清空当前时间点 1 小时之前的文件
         */
        taskRegistrar.addTriggerTask(() -> fileUploadProvider.tempClean(),
                                    triggerContext -> new PeriodicTrigger(2, TimeUnit.HOURS).nextExecutionTime(triggerContext));
    }
}
