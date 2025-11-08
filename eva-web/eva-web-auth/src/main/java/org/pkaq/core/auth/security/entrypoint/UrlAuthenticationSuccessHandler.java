package org.pkaq.core.auth.security.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.pkaq.core.log.base.BizLogSupporter;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.CookieUtils;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.RequestUtil;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8);
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

        String domain = evaConfig.getCookie().getDomain();
        int maxAge = evaConfig.getCookie().getMaxAge();
        String path = "/";

        CookieUtils.addCookie(httpServletResponse, CommonConstant.ACCESS_TOKEN_KEY, access_token, maxAge, path, domain);
        CookieUtils.addCookie(httpServletResponse, CommonConstant.REFRESH_TOKEN_KEY, refresh_token, maxAge, path, domain);
        CookieUtils.addCookie(httpServletResponse, CommonConstant.USER_KEY, URLEncoder.encode(mapper.writeValueAsString(user), StandardCharsets.UTF_8), maxAge, path, domain);

        Map<String, Object> map = HashMap.newHashMap(3);
        map.put(CommonConstant.USER_KEY, user);
        map.put(CommonConstant.ACCESS_TOKEN_KEY, access_token);
        map.put(CommonConstant.REFRESH_TOKEN_KEY, refresh_token);

        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
                .setOperateDatetime(DateUtils.now())
                .setDevice(RequestUtil.getDeivce(request))
                .setVersion(RequestUtil.getVersion(request))
                .setOperator(user.getAccount())
                .setOperateType("login");

        log.info(bizLogEntity.toString());

        bizLogSupporter.save(bizLogEntity);

        try (PrintWriter printWriter = httpServletResponse.getWriter()) {
            printWriter.write(mapper.writeValueAsString(
                            Response
                                    .success(map, CommonCodes.LOGIN_SUCCESS_WELCOME, user.getName())
                    )
            );
            printWriter.flush();
        }
    }
}
