package tech.yunyue.core.log.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.constant.LogConstant;
import tech.yunyue.core.log.events.BizLogEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * 手动异步保存操作日志工具类
 */
@Component
public class BizLogUtil {
    @Autowired
    ApplicationEventPublisher publisher;
    private static ApplicationEventPublisher eventPublisher;
    /**
     * 业务方法有事务：事务事件监听器则会在事件的指定阶段触发 为了避免在BizLogUtil.sava之前报错而无法插入错误日志
     *  以下这段代码放第一行<br/>
     * BizLogEntity bizLogEntity = new BizLogEntity();<br/>
     * BizLogUtil.sava(bizLogEntity)<br/>
     * <p/>
     *业务方法无事务：事务监听器变成普通监听器，调用则马上触发<br/>
     * 把bizLogEntity构建完成后调用BizLogUtil.sava 如下：<br/>
     * bizLogEntity.setX.setY...;<br/>
     * BizLogUtil.sava(bizLogEntity)<br/>
     * @param bizLogEntity
     */
    public static void sava(BizLogEntity bizLogEntity){
        eventPublisher.publishEvent(new BizLogEvent(bizLogEntity));
    }

    @PostConstruct
    public void init(){
        eventPublisher = publisher;
    }
}
