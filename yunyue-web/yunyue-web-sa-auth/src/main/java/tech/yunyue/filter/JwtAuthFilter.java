package tech.yunyue.filter;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Jwt;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.core.web.util.RequestUtil;
import tech.yunyue.util.TenantUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
                // 该token是否在redis中有对应的新token 旧token已经在redis中被删掉了 所以这边用新token替代
                boolean isReplace = false;
                var newToken = dao.get(authToken);
                if(StrUtil.isNotBlank(newToken)) {
                    isReplace = true;
                }

                //验证token 是否合法
                uid = (String) (isReplace ? StpUtil.getLoginIdByToken(newToken) : StpUtil.getLoginId());
                account = (String) (isReplace ? StpUtil.getExtra(newToken,"account") : StpUtil.getExtra("account"));

                //判断token是否临期且不存在上一个临期token  就刷新token
                long timeout = StpUtil.getTokenTimeout();
                if(!isReplace && timeout > 0 && timeout < jwtConfig.getThreshold()) {
                    //多个临期token的线程同时到这边 锁住
                    synchronized (authToken.intern()) {
                        // 双重监测 redis里面确实没有该token的映射 就生成一个新token
                        if(!StrUtil.isNotBlank(dao.get(authToken))) {
                            String device = StpUtil.getLoginDevice();
                            //允许并发登录时，需手动删除临期token
                            if(evaConfig.getConcurrent()) StpUtil.logout(uid,device);
                            StpUtil.login(uid, SaLoginConfig
                                    .setExtra("userId", uid)
                                    .setExtra("account", account)
                                    .setExtra("version", StpUtil.getExtra("version"))
                                    .setDevice(device));
                            //在redis中旧token映射到新token上
                            dao.set(authToken, StpUtil.getTokenValue(), timeout);
                        }
                    }
                }
                isvalid = true;
            } catch (NotLoginException e) {
				// tokne过期 返回401
                try (PrintWriter printWriter = response.getWriter()) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
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
            Map<String, ThreadUser.GrantedRoles> rolesMap = (Map<String, ThreadUser.GrantedRoles>)dao.getObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+uid);
            logger.info("checking authentication ：" + account);
            if (StrUtil.isNotBlank(account)) {
                var userStr = Optional.ofNullable((String)dao.getObject(CommonConstant.REDIS_USER_INFO_PREFIX_KEY+uid)).orElse("{}");
                ThreadUser currentUser = JSONUtil.toBean(userStr, ThreadUser.class);
                currentUser.setUserId(uid)
                        .setUserName(account)
                        .setRolesMap(rolesMap)
                        .setTenantId(TenantUtil.getTenantId(uid))
                        .setCompanyTenantId(TenantUtil.getComTenantId(uid))
                        .setModuleId(RequestUtil.getModuleId(request));
                ThreadUserHelper.setCurrentUser(currentUser);
            }
        }

        chain.doFilter(request, response);
    }
}
