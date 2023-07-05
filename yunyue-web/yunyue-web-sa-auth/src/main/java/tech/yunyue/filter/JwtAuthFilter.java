package tech.yunyue.filter;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.error.SaErrorCode;
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
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.yunyue.auth.domain.JwtUserFactory;
import tech.yunyue.auth.service.JDBCService;
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
    private final EvaConfig evaConfig;
    private final JDBCService jdbcService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        // 匿名访问url不需要校验token
        return SaRouter.match(evaConfig.getSecurity().getWebstatic()).isHit() || SaRouter.match(evaConfig.getSecurity().getAnonymous()).isHit();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
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
                    authToken = newToken;
                    //把新token写到cookie中
                    StpUtil.getStpLogic().setTokenValueToCookie(authToken, (int) evaConfig.getJwt().getAlphaTtl());
                }

                //验证token 是否合法
                uid = getLoginId(authToken);
                account = (String) StpUtil.getExtra(authToken,"account");

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
				// token过期 返回401
                try (PrintWriter printWriter = response.getWriter()) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    printWriter.write(JsonUtil.toJson(new Response<>().failure(BizCodeEnum.LOGIN_EXPIRED)));
                    printWriter.flush();
                }
                return;
            }
        }

        //把登录用户信息存到ThreadUser中
        if (isvalid) {
            var userStr = Optional.ofNullable((String)dao.getObject(CommonConstant.REDIS_USER_INFO_PREFIX_KEY+uid)).orElse("{}");
            ThreadUser currentUser = JSONUtil.toBean(userStr, ThreadUser.class);
            currentUser.setUserId(uid)
                    .setAccount(account)
                    .setRolesMap(getUserRoles(uid))
                    .setModuleId(RequestUtil.getModuleId(request));

            //启用租户则设置租户id
            if(evaConfig.getTenant().isEnable()) {
                currentUser.setTenantId(TenantUtil.getTenantId(uid));
            }
            ThreadUserHelper.setCurrentUser(currentUser);
        }

        chain.doFilter(request, response);
    }

    /**
     * 获取当前会话账号id, 如果未登录，则抛出异常
     * @return 账号id
     */
    private String getLoginId(String tokenValue) {
        String loginType = StpUtil.getLoginType();
        // 查找此token对应loginId, 如果找不到则抛出：无效token
        String loginId = (String) StpUtil.getLoginIdByToken(tokenValue);
        if(loginId == null) {
            throw NotLoginException.newInstance(loginType, NotLoginException.INVALID_TOKEN, tokenValue).setCode(SaErrorCode.CODE_11012);
        }
        // 如果是已经过期，则抛出：已经过期
        if(loginId.equals(NotLoginException.TOKEN_TIMEOUT)) {
            throw NotLoginException.newInstance(loginType, NotLoginException.TOKEN_TIMEOUT, tokenValue).setCode(SaErrorCode.CODE_11013);
        }
        // 如果是已经被顶替下去了, 则抛出：已被顶下线
        if(loginId.equals(NotLoginException.BE_REPLACED)) {
            throw NotLoginException.newInstance(loginType, NotLoginException.BE_REPLACED, tokenValue).setCode(SaErrorCode.CODE_11014);
        }
        // 如果是已经被踢下线了, 则抛出：已被踢下线
        if(loginId.equals(NotLoginException.KICK_OUT)) {
            throw NotLoginException.newInstance(loginType, NotLoginException.KICK_OUT, tokenValue).setCode(SaErrorCode.CODE_11015);
        }
        // 至此，返回loginId
        return loginId;
    }

    /**
     * 获取当前登录用户的角色
     * @param uid 账号id
     * @return 角色列表
     */
    private Map<String, ThreadUser.GrantedRoles> getUserRoles(String uid){
        SaTokenDao dao = StpUtil.getStpLogic().getSaTokenDao();
        Map<String, ThreadUser.GrantedRoles>  roles = (Map<String, ThreadUser.GrantedRoles>)dao.getObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+uid);
        if (Objects.isNull(roles)){
            // 查询数据库且将用户角色保存到redis中
            roles = JwtUserFactory.mapToGrantedAuthorities(this.jdbcService.getRoleById(uid));
            // 用户角色的存储时间设置为30天
            dao.setObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+uid, roles, evaConfig.getJwt().getBravoTtl());
        }
        return roles;
    }
}
