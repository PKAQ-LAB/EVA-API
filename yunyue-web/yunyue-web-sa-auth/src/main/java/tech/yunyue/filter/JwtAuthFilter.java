package tech.yunyue.filter;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.jwt.JwtUtil;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Jwt;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.core.web.util.RequestUtil;
import tech.yunyue.core.web.util.TenantUtil;
import tech.yunyue.core.web.util.TokenUtil;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
@Order(SaTokenConsts.ASSEMBLY_ORDER)
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final String REQUEST_ATTRIBUTES_ATTRIBUTE =
            RequestContextListener.class.getName() + ".REQUEST_ATTRIBUTES";

    private final EvaConfig evaConfig;

    private final  TenantUtil tenantUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        //往web上下文注入request和response StpUtil需要使用  抄袭RequestContextListener
        ServletRequestAttributes attributes = new ServletRequestAttributes(request, response);
        request.setAttribute(REQUEST_ATTRIBUTES_ATTRIBUTE, attributes);
        LocaleContextHolder.setLocale(request.getLocale());
        RequestContextHolder.setRequestAttributes(attributes);

        // 匿名访问url不需要校验token
        if (SaRouter.match(evaConfig.getSecurity().getAnonymous()).isHit()) {
            chain.doFilter(request, response);
            return;
        }

        Jwt jwtConfig=evaConfig.getJwt();
        var isvalid = false;

        String authToken;
        try {
            // 从Storage、请求体、cookie中获取token
            authToken = StpUtil.getTokenValue();
        } catch (Exception e) {
            authToken = null;
            logger.warn(e);
        }
        String uid = null;
        String account = null;
        SaTokenDao dao = StpUtil.getStpLogic().getSaTokenDao();

        if (StrUtil.isNotBlank(authToken)) {
            try {
                //验证token 是否合法
                uid = (String) StpUtil.getLoginId();
                account = (String) StpUtil.getExtra("account");

                //判断token是否临期  临期就刷新token
                long timeout = StpUtil.getTokenTimeout();
                if(timeout > 0 && timeout < jwtConfig.getThreshold()) {
                    // 放入缓存、cookie 且通过事件把旧token清除了
                    StpUtil.login(uid, SaLoginConfig
                            .setExtra("userId", uid)
                            .setExtra("account", account)
                            .setExtra("version", StpUtil.getExtra("version"))
                            .setDevice(StpUtil.getLoginDevice()));
                }
                isvalid = true;
            } catch (NotLoginException e) {
                logger.warn(e.getMessage(), e);
                try (PrintWriter printWriter = response.getWriter()) {
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    printWriter.write(JsonUtil.toJson(new Response().failure(BizCodeEnum.LOGIN_EXPIRED)));
                    printWriter.flush();
                }
                return;
            }
        }

        //把登录用户信息存到ThreadUser中
        if (isvalid) {
            StpUtil.getRoleList();
            var tenantId = tenantUtil.getTenantId(uid);
            List<String> roles = (List<String>)dao.getObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+uid);
            logger.info("checking authentication ：" + account);
            if (StrUtil.isNotBlank(account)) {
                ThreadUser currentUser = new ThreadUser().setUserId(uid)
                        .setUserName(account)
                        .setRoles(roles.toArray(new String[0]))
                        .setTenantId(tenantId)
                        .setModuleId(RequestUtil.getModuleId(request));
                ThreadUserHelper.setCurrentUser(currentUser);
            }
        }

        chain.doFilter(request, response);
    }
}
