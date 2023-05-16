package tech.yunyue.listener;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 *  Sa-Token 侦听器
 */
@Component
public class ReplacedSaTokenListener extends SaTokenListenerForSimple {

    /**
     * 用户token过期 但是ActivityTimeout还没到期 直接生成一个新token
     * 会触发被顶下线的事件 在该事件中删掉旧token的一切信息
     * @param loginType 账号类别
     * @param loginId 账号id
     * @param tokenValue token值
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        // 删掉旧token的一切缓存
        StpUtil.logoutByTokenValue(tokenValue);
    }
}
