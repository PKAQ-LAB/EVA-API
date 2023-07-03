package tech.yunyue.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

        //鉴权
        SaRouter.match("/**",r->{
            // 当前会话是否登录
            if(Objects.isNull(ThreadUserHelper.getCurrentUser()) || !StringUtils.hasText(ThreadUserHelper.getUserId())){
                SaHolder.getResponse().setStatus(HttpStatus.UNAUTHORIZED.value());
                BizCodeEnum.LOGIN_EXPIRED.newException();
            }
            //当前资源是否需要鉴权
            SaRouter.match(evaConfig.getSecurity().getPermit()).stop();
            //权限验证
            if (evaConfig.getResourcePermission().isEnable() && !StpUtil.hasPermission(ThreadUserHelper.getUserId(), SaHolder.getRequest().getRequestPath())) {
                BizCodeEnum.PERMISSION_DENY.newException();
            }
        });

    }
}
