package io.nerv.handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nerv.core.bizlog.base.BizLogSupporter;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.token.jwt.JwtUtil;
import io.nerv.core.token.util.TokenUtil;
import io.nerv.core.util.RequestUtil;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;

/**
 * 自定义登录成功处理器
 */
@Component
public class UrlAuthenticationSuccessHandler extends WebFilterChainServerAuthenticationSuccessHandler {

    private final static String clientId = "";
    private final static String clientSecrect = "";

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

//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse httpServletResponse,
//                                        Authentication authentication) throws IOException {
//        var cacheToken = evaConfig.getJwt().isPersistence();
//
//        httpServletResponse.setCharacterEncoding("UTF-8");
//        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//
//        //表单输入的用户名
//        JwtUserDetail user = (JwtUserDetail) authentication.getPrincipal();
//        // 签发 access_token -> ALPHA
//        String access_token = jwtUtil.build(evaConfig.getJwt().getAlphaTtl(), user.getAccount());
//        // 签发 refresh_token -> BRAVO
//        String refresh_token = jwtUtil.build(evaConfig.getJwt().getBravoTtl(), user.getAccount());
//
//        // token放入缓存
//        if (cacheToken) {
//            tokenUtil.saveToken(user.getAccount(), tokenUtil.buildCacheValue(request, user.getAccount(), access_token));
//        }
//
//        ServletUtil.addCookie(httpServletResponse,
//                CommonConstant.ACCESS_TOKEN_KEY,
//                access_token,
//                evaConfig.getCookie().getMaxAge(),
//                "/",
//                evaConfig.getCookie().getDomain());
//
//        ServletUtil.addCookie(httpServletResponse,
//                CommonConstant.REFRESH_TOKEN_KEY,
//                refresh_token,
//                evaConfig.getCookie().getMaxAge(),
//                "/",
//                evaConfig.getCookie().getDomain());
//
//        ServletUtil.addCookie(httpServletResponse,
//                CommonConstant.USER_KEY,
//                URLEncoder.encode(mapper.writeValueAsString(user), CharsetUtil.UTF_8),
//                evaConfig.getCookie().getMaxAge(),
//                "/",
//                evaConfig.getCookie().getDomain());
//
//        Map<String, Object> map = new HashMap<>(2);
//        map.put("user", user);
//        map.put("tk_alpha", access_token);
//        map.put("tk_bravo", refresh_token);
//
//        BizLogEntity bizLogEntity = new BizLogEntity();
//        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
//                .setOperateDatetime(DateUtil.now())
//                .setDevice(requestUtil.getDeivce(request))
//                .setVersion(requestUtil.getVersion(request))
//                .setOperator(user.getAccount())
//                .setOperateType("login");
//
//        bizLogSupporter.save(bizLogEntity);
//
//        try(PrintWriter printWriter = httpServletResponse.getWriter()){
//            printWriter.write(mapper.writeValueAsString(
//                new Response()
//                        .success(map, StrUtil.format( BizCodeEnum.LOGIN_SUCCESS_WELCOME.getName(), user.getName() ) )
//                )
//            );
//            printWriter.flush();
//        }
//    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpResponse response = exchange.getResponse();

        //设置headers
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

        //设置body
        Response result = new Response();

        byte[] dataBytes={};
        ObjectMapper mapper = new ObjectMapper();

        try {
            User user=(User)authentication.getPrincipal();

            byte[] authorization=(clientId+":"+clientSecrect).getBytes();
            String token= Base64.getEncoder().encodeToString(authorization);
            httpHeaders.add(HttpHeaders.AUTHORIZATION, token);

//            result.setData(userDetails);
//            dataBytes=mapper.writeValueAsBytes(wsResponse);
        }
        catch (Exception ex){
            ex.printStackTrace();
//            JsonObject result = new JsonObject();
//            result.addProperty("status", MessageCode.COMMON_FAILURE.getCode());
//            result.addProperty("message", "授权异常");
//            dataBytes=result.toString().getBytes();
        }
//        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(dataBytes);
//        return response.writeWith(Mono.just(bodyDataBuffer));

        return  null;

    }
}
