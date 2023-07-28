package tech.yunyue.core.log.config;

import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.ErrorlogEntity;
import tech.yunyue.core.log.base.LogSupporter;
import tech.yunyue.core.log.events.BizLogEvent;
import tech.yunyue.core.log.events.ErrorLogEvent;

// todo  我是想统一定义好名字  这样子就算有别的实现也是固定这两个名字
public interface LogConfig {

    <T extends BizLogEntity> LogSupporter<T, BizLogEvent> bizLogSupporter();

    <T extends ErrorlogEntity> LogSupporter<T, ErrorLogEvent> errorLogSupporter();
}
