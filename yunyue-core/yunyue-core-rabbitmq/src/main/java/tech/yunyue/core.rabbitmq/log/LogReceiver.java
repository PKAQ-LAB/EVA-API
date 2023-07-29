package tech.yunyue.core.rabbitmq.log;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.util.LogHelper;

/**
 * 业务日志接收器
 */
@ConditionalOnExpression("${eva.bizlog.mq-enabled:false} || ${eva.errorlog.mq-enabled:false}")
@Component
public class LogReceiver {
    /**
     * 操作日志
     */
    @RabbitHandler
    @RabbitListener(queues = "bizlog")
    public void process(BizLogEntity bizLogEntity) {
        // 保存到数据库中
        LogHelper.save(bizLogEntity);
    }

    /**
     * 错误日志
     */
    @RabbitListener(queues = "errorlog")
    @RabbitHandler
    public void process(ErrorlogEntity errorlogEntity) {
        // 保存到数据库中
        LogHelper.save(errorlogEntity);
    }
}
