package tech.yunyue.listener;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.event.BizEvent;

import java.util.List;

/**
 *  Sa-Token 侦听器
 */
@Component
public class SaTokenListener extends SaTokenListenerForSimple {
    @Autowired
    SaTokenConfig saTokenConfig;
    @Autowired
    CacheManager cacheManager;


    /**
     * 监听踢出用户事件 并根据id踢出用户
     * 先同步处理
     */
    @EventListener
    public void listenerCommit(BizEvent event) {
        BizEvent.Event evt = (BizEvent.Event) event.getSource();
        if(!CommonConstant.KICK_USER_EVENT.equals(evt.getEventName())) return;

        List<String> idList = (List<String>) evt.getObj();
        idList.stream().forEach(id -> {
            //删掉access_token
            StpUtil.logout(id);
            //删掉refresh_token
            saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
            StpUtil.logout(id);
            saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY); //改回来
            Cache cache = cacheManager.getCache(CommonConstant.CACHE_USERDATA);
            // 删除缓存中用户角色
            cache.evict(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+id);
            // 删除缓存中的用户信息
            cache.evict(CommonConstant.REDIS_USER_INFO_PREFIX_KEY+id);
        });
    }
}
