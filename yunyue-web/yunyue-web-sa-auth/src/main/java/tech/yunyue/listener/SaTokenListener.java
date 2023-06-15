package tech.yunyue.listener;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.event.KickUserEvent;

import java.util.List;

/**
 *  Sa-Token 侦听器
 */
@Component
public class SaTokenListener extends SaTokenListenerForSimple {
    @Autowired
    SaTokenConfig saTokenConfig;

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


    /**
     * 监听踢出用户事件 并根据id踢出用户
     * 先同步处理
     */
    @EventListener
    public void listenerCommit(KickUserEvent event) {
        List<String> idList = (List<String>) event.getSource();
        for (String id : idList) {
            //删掉access_token
            StpUtil.logout(id);
            //删掉refresh_token
            saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
            StpUtil.logout(id);
            saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY); //改回来
            // 删除redis中用户角色
            SaTokenDao dao = StpUtil.getStpLogic().getSaTokenDao();
            dao.deleteObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+id);
            // 删除redis中的用户信息
            dao.delete(CommonConstant.REDIS_USER_INFO_PREFIX_KEY+id);
        }
    }
}
