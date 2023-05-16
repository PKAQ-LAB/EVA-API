package tech.yunyue.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUserHelper;

import java.util.Objects;

/**
 * 路由认证策略 每次请求都会执行
 */
@Component
public class FilterAuthStrategy implements SaFilterAuthStrategy {
    @Autowired
    EvaConfig evaConfig;

    @Override
    public void run(Object o) {
        //排除可以匿名访问的接口
        SaRouter.match(evaConfig.getSecurity().getAnonymous()).stop();
        //  SaRouter.match("/**", "/auth/login", r -> StpUtil.checkLogin()); login进来为false 别的进来都是true

        //不是匿名访问的接口 但是不一定携带了token 没有携带token直接报错 携带token则鉴权
        SaRouter.match("/**",r->{
            // 当前会话是否经过jwtFilter的token验证
            if(Objects.isNull(ThreadUserHelper.getCurrentUser())){
                //说明没带token 即用户没登录
                BizCodeEnum.LOGIN_EXPIRED.newException();
            }

            //权限验证
            if (!evaConfig.getResourcePermission().isEnable()) return;
            boolean granted = StpUtil.hasPermission(SaHolder.getRequest().getRequestPath());
            if (!granted) {
                BizCodeEnum.PERMISSION_DENY.newException();
            }
        });

    }
}
