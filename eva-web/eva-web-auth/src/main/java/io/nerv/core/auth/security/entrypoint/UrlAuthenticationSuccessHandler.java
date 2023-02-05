package io.nerv.core.auth.security.entrypoint;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nerv.core.auth.domain.JwtUserDetail;
import io.nerv.core.auth.util.CacheTokenUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.jwt.JwtUtil;
import io.nerv.core.log.base.BizLogEntity;
import io.nerv.core.log.base.BizLogSupporter;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.properties.EvaConfig;
import io.nerv.core.web.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义登录成功处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    private final EvaConfig evaConfig;

    private final BizLogSupporter bizLogSupporter;

    private final CacheTokenUtil tokenUtil;

    private final ObjectMapper mapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        var cacheToken = evaConfig.getJwt().isPersistence();

        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        //表单输入的用户名
        JwtUserDetail user = (JwtUserDetail) authentication.getPrincipal();
        // 签发 access_token -> ALPHA
        String access_token = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), user.getId(), user.getAccount());
        // 签发 refresh_token -> BRAVO
        String refresh_token = jwtUtil.build(evaConfig.getJwt().getBravoTtl(), user.getId(), user.getAccount());

        // token放入缓存
        if (cacheToken) {
            tokenUtil.saveToken(user.getId(), tokenUtil.buildCacheValue(request, user.getId(), access_token));
        }

        JakartaServletUtil.addCookie(httpServletResponse,
                CommonConstant.ACCESS_TOKEN_KEY,
                access_token,
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());

        JakartaServletUtil.addCookie(httpServletResponse,
                CommonConstant.REFRESH_TOKEN_KEY,
                refresh_token,
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());

        JakartaServletUtil.addCookie(httpServletResponse,
                CommonConstant.USER_KEY,
                URLEncoder.encode(mapper.writeValueAsString(user), CharsetUtil.UTF_8),
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());

        Map<String, Object> map = new HashMap<>(2);
        map.put(CommonConstant.USER_KEY, user);
        map.put(CommonConstant.ACCESS_TOKEN_KEY, access_token);
        map.put(CommonConstant.REFRESH_TOKEN_KEY, refresh_token);

        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
                .setOperateDatetime(DateUtil.now())
                .setDevice(RequestUtil.getDeivce(request))
                .setVersion(RequestUtil.getVersion(request))
                .setOperator(user.getAccount())
                .setOperateType("login");

        log.info(bizLogEntity.toString());

        bizLogSupporter.save(bizLogEntity);

        try (PrintWriter printWriter = httpServletResponse.getWriter()) {
            printWriter.write(mapper.writeValueAsString(
                            new Response()
                                    .success(map, BizCodeEnum.LOGIN_SUCCESS_WELCOME, user.getName())
                    )
            );
            printWriter.flush();
        }
    }
}
