package io.nerv.security.entrypoint;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nerv.core.bizlog.base.BizLogEntity;
import io.nerv.core.bizlog.base.BizLogSupporter;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.security.domain.JwtUserDetail;
import io.nerv.core.token.jwt.JwtUtil;
import io.nerv.core.token.util.TokenUtil;
import io.nerv.core.util.RequestUtil;
import io.nerv.properties.EvaConfig;
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
public class UrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private BizLogSupporter bizLogSupporter;

    @Autowired
    private RequestUtil requestUtil;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper mapper;

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
        String access_token = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), user.getAccount());
        // 签发 refresh_token -> BRAVO
        String refresh_token = jwtUtil.build(evaConfig.getJwt().getBravoTtl(), user.getAccount());

        // token放入缓存
        if (cacheToken) {
            tokenUtil.saveToken(user.getAccount(), tokenUtil.buildCacheValue(request, user.getAccount(), access_token));
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
        map.put("user", user);
        map.put("tk_alpha", access_token);
        map.put("tk_bravo", refresh_token);

        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
                .setOperateDatetime(DateUtil.now())
                .setDevice(requestUtil.getDeivce(request))
                .setVersion(requestUtil.getVersion(request))
                .setOperator(user.getAccount())
                .setOperateType("login");

        bizLogSupporter.save(bizLogEntity);

        try(PrintWriter printWriter = httpServletResponse.getWriter()){
            printWriter.write(mapper.writeValueAsString(
                new Response()
                        .success(map, StrUtil.format( BizCodeEnum.LOGIN_SUCCESS_WELCOME.getName(), user.getName() ) )
                )
            );
            printWriter.flush();
        }
    }
}
