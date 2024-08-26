package io.nerv.core.log.pointcut;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.nerv.core.log.annotation.BizLog;
import io.nerv.core.log.base.BizLogEntity;
import io.nerv.core.log.base.BizLogSupporter;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.core.util.I18NHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步记录日志
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class BizLogger {
    // 国际化文案
    private final I18NHelper i18NHelper;

    private final BizLogSupporter bizLogSupporter;

    @Async
    public void write(BizLogEntity bizLogEntity, BizLog bizlog){

        bizLogEntity.setOperator(ThreadUserHelper.getUserName());

        // 获取是否有i18n文案
        String msg = bizlog.msg();
        var message = i18NHelper.getMessage(msg, msg);
        if (CharSequenceUtil.isBlank(message)) {
            message = bizlog.description();
        }

        bizLogEntity.setDescription(message)
                    .setOperateDatetime(DateUtil.now())
                    .setOperateType(bizlog.operateType().getCode());

        this.bizLogSupporter.save(bizLogEntity);
    }
}
