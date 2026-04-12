package org.pkaq.core.auth.security.entrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.domain.JwtUserDetail;
import org.pkaq.core.auth.util.CacheTokenUtil;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.jwt.JwtUtil;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.core.auth.security.vo.LoginSuccessVo;
import org.pkaq.core.auth.security.vo.LoginUserInfoVo;
import org.pkaq.web.core.utils.CookieUtils;
import org.pkaq.web.core.utils.RequestUtil;
import org.pkaq.web.core.utils.ResponseUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 自定义登录成功处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    private final EvaConfig evaConfig;

    private final LogSupporter logSupporter;

    private final CacheTokenUtil tokenUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        var cacheToken = evaConfig.getJwt().isPersistence();


        //表单输入的用户名
        JwtUserDetail user = (JwtUserDetail) authentication.getPrincipal();
        // 签发 access_token -> ALPHA（含角色ID和权限版本号）
        String access_token = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), user.getId(), user.getAccount(), user.getRoleIds(), user.getPermVer() != null ? user.getPermVer() : 0L);
        // 签发 refresh_token -> BRAVO（含角色ID和权限版本号）
        String refresh_token = jwtUtil.build(evaConfig.getJwt().getBravoTtl(), user.getId(), user.getAccount(), user.getRoleIds(), user.getPermVer() != null ? user.getPermVer() : 0L);

        // token放入缓存
        if (cacheToken) {
            tokenUtil.saveToken(user.getId(), tokenUtil.buildCacheValue(request, user.getId(), access_token));
        }

        String domain = evaConfig.getCookie().getDomain();
        int maxAge = evaConfig.getCookie().getMaxAge();
        String path = "/";

        LoginUserInfoVo userInfo = buildLoginUserInfo(user);
        LoginSuccessVo loginSuccessVo = new LoginSuccessVo();
        loginSuccessVo.setUserInfo(userInfo);
        loginSuccessVo.setAccessToken(access_token);
        loginSuccessVo.setRefreshToken(refresh_token);

        CookieUtils.addCookie(httpServletResponse, CommonConstant.ACCESS_TOKEN_KEY, access_token, maxAge, path, domain);
        CookieUtils.addCookie(httpServletResponse, CommonConstant.REFRESH_TOKEN_KEY, refresh_token, maxAge, path, domain);
        CookieUtils.addCookie(httpServletResponse, CommonConstant.USER_KEY, URLEncoder.encode(JsonUtil.toJson(userInfo), StandardCharsets.UTF_8), maxAge, path, domain);

        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
                .setOperateDatetime(DateUtils.now())
                .setDevice(RequestUtil.getDeivce(request))
                .setVersion(RequestUtil.getVersion(request))
                .setOperator(user.getAccount())
                .setOperateType("login");

        log.info(bizLogEntity.toString());

        logSupporter.save(bizLogEntity);

        ResponseUtil.write(httpServletResponse, Response
                .success(loginSuccessVo, CommonCodes.LOGIN_SUCCESS_WELCOME, user.getName()));
    }

    /**
     * 构造登录成功后返回的轻量用户信息。
     */
    private LoginUserInfoVo buildLoginUserInfo(JwtUserDetail user) {
        LoginUserInfoVo userInfo = new LoginUserInfoVo();
        userInfo.setId(user.getId());
        userInfo.setAccount(user.getAccount());
        userInfo.setUsername(user.getUsername());
        userInfo.setDeptId(user.getDeptId());
        userInfo.setDeptName(user.getDeptName());
        userInfo.setName(user.getName());
        userInfo.setNickName(user.getNickName());
        userInfo.setAuthorities(extractAuthorities(user));
        return userInfo;
    }

    /**
     * 提取角色编码列表。
     */
    private List<String> extractAuthorities(JwtUserDetail user) {
        if (user.getAuthorities() == null) {
            return List.of();
        }
        return user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
