package tech.yunyue.filter;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.jwt.exception.SaJwtException;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.text.CharSequenceUtil;
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
import tech.yunyue.auth.service.JDBCService;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Jwt;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.core.web.util.RequestUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static cn.dev33.satoken.exception.NotLoginException.*;

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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 匿名访问url不需要校验token
        return SaRouter.match(evaConfig.getSecurity().getWebstatic()).isHit() || SaRouter.match(evaConfig.getSecurity().getAnonymous()).isHit();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (evaConfig.isOpen()) {
            chain.doFilter(request, response);
            return;
        }

        Jwt jwtConfig = evaConfig.getJwt();
        var isvalid = false;
        String authToken;
        try {
            // 从Storage、请求体、cookie中获取token
            authToken = StpUtil.getTokenValue();
            logger.warn("-线程：%s----------Storage、请求体、cookie中获取token【%s】--------用户信息%s-------".formatted(Thread.currentThread(), authToken, JSONUtil.parseObj(ThreadUserHelper.getAccount())));
            if (CharSequenceUtil.isBlank(authToken) && CharSequenceUtil.isNotEmpty(ThreadUserHelper.getUserId())) {
                logger.warn("线程：%s出现问题了------------".formatted(Thread.currentThread()));
            }
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
                if (StrUtil.isNotBlank(newToken)) {
                    isReplace = true;
                    authToken = newToken;
                    // 把新token写到cookie中
                    StpUtil.getStpLogic().setTokenValueToCookie(authToken, (int) evaConfig.getJwt().getAlphaTtl());
                }

                // 验证token 是否合法
                uid = getLoginId(authToken);
                logger.warn("线程：%s-----------经过处理的当前token为【%s】，用户id为【%s】---------------".formatted(Thread.currentThread(), authToken, uid));
                account = (String) StpUtil.getExtra(authToken, "account");

                // 判断token是否临期且不存在上一个临期token  就刷新token
                long timeout = StpUtil.getTokenTimeout();
                if (!isReplace && timeout > 0 && timeout < jwtConfig.getThreshold()) {
                    // 多个临期token的线程同时到这边 锁住
                    synchronized (authToken.intern()) {
                        // 双重监测 redis里面确实没有该token的映射 就生成一个新token
                        if (!StrUtil.isNotBlank(dao.get(authToken))) {
                            String device = StpUtil.getLoginDevice();
                            // 允许并发登录时，需手动删除临期token
                            if (evaConfig.getConcurrent()) StpUtil.logout(uid, device);
                            StpUtil.login(uid, SaLoginConfig
                                    .setExtra("userId", uid)
                                    .setExtra("account", account)
                                    .setExtra("version", StpUtil.getExtra("version"))
                                    .setDevice(device));
                            // 在redis中旧token映射到新token上
                            dao.set(authToken, StpUtil.getTokenValue(), timeout);
                        }
                    }
                }
                isvalid = true;
            } catch (NotLoginException | SaJwtException e) {
                // token过期 返回401
                try (PrintWriter printWriter = response.getWriter()) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    // 需要单独提示的登录异常
                    var otherExceptionMap = Map.of(
                            BE_REPLACED, BizCodeEnum.LOGIN_REPLACED
                    );
                    String type = (e instanceof NotLoginException exception) ? exception.getType() : "";
                    printWriter.write(JsonUtil.toJson(new Response<>().failure(otherExceptionMap.getOrDefault(type, BizCodeEnum.LOGIN_EXPIRED))));
                    printWriter.flush();
                }
                return;
            }
        }

        // 把登录用户信息存到ThreadUser中
        if (isvalid) {
            ThreadUser currentUser = JSONUtil.toBean(this.jdbcService.loadUserById(uid), ThreadUser.class);
            currentUser.setUserId(uid)
                    .setAccount(account)
                    .setRolesMap(this.jdbcService.getRoleById(uid))
                    .setModuleId(RequestUtil.getModuleId(request))
                    .setModuleCode(RequestUtil.getModuleCode(request))
                    .setDevice(RequestUtil.getDeivce(request))
                    .setVersion(RequestUtil.getVersion(request));

            // 禁用租户设置租户id为null
            if (!evaConfig.getTenant().isEnable()) {
                currentUser.setTenantId(null);
                currentUser.setTenantCode(null);
            }
            ThreadUserHelper.setCurrentUser(currentUser);
        }
        logger.warn("last------------线程：%s出现token%s------用户信息%s------------".formatted(Thread.currentThread(), authToken, ThreadUserHelper.getUserId()));
        chain.doFilter(request, response);
    }

    /**
     * 获取当前会话账号id, 如果未登录，则抛出异常
     *
     * @return 账号id
     */
    private String getLoginId(String tokenValue) {
        String loginType = StpUtil.getLoginType();
        // 查找此token对应loginId, 如果找不到则抛出：无效token
        String loginId = StpUtil.getStpLogic().getLoginIdNotHandle(tokenValue);
        if (SaFoxUtil.isEmpty(loginId)) {
            throw NotLoginException.newInstance(loginType, INVALID_TOKEN, INVALID_TOKEN_MESSAGE, tokenValue).setCode(SaErrorCode.CODE_11012);
        }
        // 4、如果这个 token 指向的是值是：过期标记，则抛出：token 已过期
        if (loginId.equals(NotLoginException.TOKEN_TIMEOUT)) {
            throw NotLoginException.newInstance(loginType, TOKEN_TIMEOUT, TOKEN_TIMEOUT_MESSAGE, tokenValue).setCode(SaErrorCode.CODE_11013);
        }

        // 5、如果这个 token 指向的是值是：被顶替标记，则抛出：token 已被顶下线
        if (loginId.equals(NotLoginException.BE_REPLACED)) {
            throw NotLoginException.newInstance(loginType, BE_REPLACED, BE_REPLACED_MESSAGE, tokenValue).setCode(SaErrorCode.CODE_11014);
        }

        // 6、如果这个 token 指向的是值是：被踢下线标记，则抛出：token 已被踢下线
        if (loginId.equals(NotLoginException.KICK_OUT)) {
            throw NotLoginException.newInstance(loginType, KICK_OUT, KICK_OUT_MESSAGE, tokenValue).setCode(SaErrorCode.CODE_11015);
        }
        // 至此，返回loginId
        return loginId;
    }
}
