package tech.yunyue.interceptor;

import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import tech.yunyue.auth.service.JDBCService;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.web.util.RequestUtil;

import java.util.Map;

import static cn.dev33.satoken.exception.NotLoginException.*;

/**
 * @author 茂茂AdamEve
 * WebSocket鉴权拦截器，用于在WebSocket握手阶段执行鉴权逻辑。
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private static final String AUTH_FAIL_HEARD = "X-WebSocket-Error";
    private final EvaConfig evaConfig;
    private final JDBCService jdbcService;

    /**
     * 在WebSocket握手前执行鉴权逻辑。
     *
     * @param request    握手请求
     * @param response   握手响应
     * @param wsHandler  WebSocket处理器
     * @param attributes 握手阶段的属性集合
     * @return 如果鉴权成功，返回true；否则返回false
     * @throws Exception 发生异常时抛出
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // 从Storage、请求体、cookie中获取token
            var authToken = StpUtil.getTokenValue();
            if (CharSequenceUtil.isBlank(authToken)) return false;
            // 验证token 是否合法
            var uid = getLoginId(authToken);
            var account = (String) StpUtil.getExtra(authToken, "account");
            ThreadUser currentUser = JSONUtil.toBean(this.jdbcService.loadUserById(uid), ThreadUser.class);
            currentUser.setUserId(uid)
                    .setAccount(account)
                    .setRolesMap(this.jdbcService.getRoleById(uid))
                    .setModuleId(RequestUtil.getModuleId(((ServletServerHttpRequest) request).getServletRequest()))
                    .setModuleCode(RequestUtil.getModuleCode(((ServletServerHttpRequest) request).getServletRequest()));
            // 禁用租户设置租户id为null
            if (!evaConfig.getTenant().isEnable()) {
                currentUser.setTenantId(null);
                currentUser.setTenantCode(null);
            }
            ThreadUserHelper.setCurrentUser(currentUser);
            return true;
        } catch (NotLoginException e) {
            response.getHeaders().add(AUTH_FAIL_HEARD, StrUtil.utf8Str(e.getMessage()));
            return false;
        }
    }

    /**
     * 握手成功后的处理，此处不执行具体逻辑。
     *
     * @param request   握手请求
     * @param response  握手响应
     * @param wsHandler WebSocket处理器
     * @param exception 握手过程中的异常（如果有）
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, @Nullable Exception exception) {
        // 握手成功后的处理，此处不执行具体逻辑
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
