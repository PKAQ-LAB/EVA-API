package tech.yunyue.core.log.events;

import tech.yunyue.core.log.base.BizLogEntity;


/**
 * 系统日志事件
 *
 * @author PKAQ
 */
public class BizLogEvent extends LogEvent<BizLogEntity> {

    public BizLogEvent(BizLogEntity entity) {
        super(entity);
    }
}
