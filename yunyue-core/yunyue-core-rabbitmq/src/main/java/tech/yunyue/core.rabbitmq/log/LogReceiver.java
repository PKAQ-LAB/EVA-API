package tech.yunyue.core.rabbitmq.log;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.util.LogHelper;

/**
 * 业务日志接收器
 */
@ConditionalOnExpression("${eva.syslog.mq-enabled:false}")
@Component
@AllArgsConstructor
public class LogReceiver {
    private final LogHelper logHelper;

    /**
     * 操作日志
     */
    @RabbitHandler
    @RabbitListener(queues = "bizlog")
    public void process(BizLogEntity bizLogEntity) {
        // 保存到数据库中
        logHelper.getBizLogSupporter().save(bizLogEntity);
    }

    /**
     * 错误日志
     */
    @RabbitListener(queues = "errorlog")
    @RabbitHandler
    public void process(ErrorlogEntity errorlogEntity) {
        // 保存到数据库中
        logHelper.getErrorLogSupporter().save(errorlogEntity);
    }

    /**
     * 错误日志
     */
    @RabbitListener(queues = "loginlog")
    @RabbitHandler
    public void process(LoginlogEntity loginlogEntity) {
        // 保存到数据库中
        logHelper.getLoginLogSupporter().save(loginlogEntity);
    }
}
