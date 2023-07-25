package tech.yunyue.core.rabbitmq.log;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.BizLogSupporter;

/**
 * 业务日志接收器
 */
@ConditionalOnExpression("${eva.bizlog.mq-enabled:false}")
@Component
public class BizLogReceiver {
    @Autowired
    BizLogSupporter bizLogSupporter;

    /**
     * 操作日志
     */
    @RabbitHandler
    @RabbitListener(queues = "bizlog")
    public void process(BizLogEntity bizLogEntity) {
        // 保存到数据库中
        bizLogSupporter.save(bizLogEntity);
    }
}
