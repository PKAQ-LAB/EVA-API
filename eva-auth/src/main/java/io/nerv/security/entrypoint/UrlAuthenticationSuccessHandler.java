package io.nerv.security.entrypoint;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import io.nerv.core.bizlog.base.BizLogEntity;
import io.nerv.core.bizlog.base.BizLogSupporter;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.security.domain.JwtUserDetail;
import io.nerv.properties.EvaConfig;
import io.nerv.core.token.jwt.JwtUtil;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {

        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        //表单输入的用户名
        JwtUserDetail user = (JwtUserDetail) authentication.getPrincipal();
        // 签发token
        String token = jwtUtil.build(evaConfig.getJwt().getTtl(), user.getAccount());

        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.TOKEN_KEY,
                token,
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());


        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.USER_KEY,
                URLEncoder.encode(JSON.toJSONString(user), CharsetUtil.UTF_8),
                evaConfig.getCookie().getMaxAge(),
                "/",
                evaConfig.getCookie().getDomain());

        Map<String, Object> map = new HashMap<>(2);
        map.put("user", user);
        map.put("token", token);

        String device = request.getHeader("device");
        String version = request.getHeader("version");

        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
                .setOperateDatetime(DateUtil.now())
                .setDevice(device)
                .setVersion(version)
                .setOperator(user.getAccount())
                .setOperateType("login");

        bizLogSupporter.save(bizLogEntity);


        try(PrintWriter printWriter = httpServletResponse.getWriter()){
            printWriter.write(JSON.toJSONString(
                    new Response()
                            .success(map, StrUtil.format( BizCodeEnum.LOGIN_SUCCESS_WELCOME.getName(), user.getName() ) )
                    )
            );
            printWriter.flush();
        }
    }
}
