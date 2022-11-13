package io.nerv.auth.security.entrypoint;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nerv.auth.util.CacheTokenUtil;
import io.nerv.common.bizlog.base.BizLogEntity;
import io.nerv.common.bizlog.base.BizLogSupporter;
import io.nerv.common.constant.CommonConstant;
import io.nerv.common.enums.BizCodeEnum;
import io.nerv.common.jwt.JwtUtil;
import io.nerv.common.mvc.vo.Response;
import io.nerv.common.util.RequestUtil;
import io.nerv.auth.domain.JwtUserDetail;
import io.nerv.properties.EvaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义登录成功处理器
 */
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

        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.ACCESS_TOKEN_KEY,
                access_token,
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());

        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.REFRESH_TOKEN_KEY,
                refresh_token,
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());

        ServletUtil.addCookie(httpServletResponse,
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

        bizLogSupporter.save(bizLogEntity);

        try(PrintWriter printWriter = httpServletResponse.getWriter()){
            printWriter.write(mapper.writeValueAsString(
                new Response()
                        .success(map, BizCodeEnum.LOGIN_SUCCESS_WELCOME, user.getName())
                )
            );
            printWriter.flush();
        }
    }
}
