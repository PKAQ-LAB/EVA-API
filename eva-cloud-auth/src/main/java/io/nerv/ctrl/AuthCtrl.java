package io.nerv.ctrl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.util.RedisUtil;
import io.nerv.exception.AuthException;
import io.nerv.jwt.JwtToken;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthCtrl {

    private final EvaConfig evaConfig;
    private final TokenEndpoint tokenEndpoint;

    private final RedisUtil redisUtil;

    private final ClientDetailsService clientDetailsService;

    // 配置的clientId
    @Value("${eva.oauth2.clientId}")
    private String clientId;

    public AuthCtrl(EvaConfig evaConfig, TokenEndpoint tokenEndpoint, RedisUtil redisUtil, ClientDetailsService clientDetailsService) {
        this.evaConfig = evaConfig;
        this.tokenEndpoint = tokenEndpoint;
        this.redisUtil = redisUtil;
        this.clientDetailsService = clientDetailsService;
    }

    /**
     * Oauth2登录认证
     * 前端无需传递clientid clientsecrect 以及granttype
     */
    @PostMapping(value = "/login")
    public Response login(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        var client = clientDetailsService.loadClientByClientId(clientId);

        if (null == client){
            throw new AuthException(BizCodeEnum.PERMISSION_EXPIRED);
        }
        parameters.put("grant_type", "password");

        User clientUser= new User(client.getClientId(),client.getClientSecret(), new ArrayList<>());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(clientUser,null, new ArrayList<>());

        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(token, parameters).getBody();

        JwtToken oauth2TokenDto = JwtToken.builder()
                .accessToken(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue()).build();
        return new Response().success(oauth2TokenDto);
    }

    /**
     * Oauth2 刷新token
     * 前端无需传递clientid clientsecrect 以及granttype
     */
    @PostMapping(value = "/refresh")
    public Response refresh(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        var client = clientDetailsService.loadClientByClientId(clientId);

        if (null == client){
            throw new AuthException(BizCodeEnum.PERMISSION_EXPIRED);
        }
        parameters.put("grant_type", "refresh_token");

        User clientUser= new User(client.getClientId(),client.getClientSecret(), new ArrayList<>());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(clientUser,null, new ArrayList<>());

        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(token, parameters).getBody();

        JwtToken oauth2TokenDto = JwtToken.builder()
                .accessToken(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue()).build();
        return new Response().success(oauth2TokenDto);
    }

    /**
     * 退出登录需要需要登录的一点思考：
     * 1、如果不需要登录，那么在调用接口的时候就需要把token传过来，且系统不校验token有效性，此时如果系统被攻击，不停的大量发送token，最后会把redis充爆
     * 2、如果调用退出接口必须登录，那么系统会调用token校验有效性，refresh_token通过参数传过来加入黑名单
     * 综上：选择调用退出接口需要登录的方式
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Response logout(HttpServletRequest request) {
        String tokenKey = evaConfig.getJwt().getHeader();

        //认证信息从Header 或 请求参数 或cookies 中获取
        String token = request.getHeader(tokenKey);

        if (StrUtil.isBlank(token)){
            token = request.getParameter(tokenKey);
        }

        if (StrUtil.isBlank(token)){
            token = ServletUtil.getCookie(request, tokenKey).getValue();
        }

        String refreshToken = request.getParameter(CommonConstant.REFRESH_TOKEN_KEY);
        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        // 将token和refresh_token同时加入黑名单
        String[] tokenArray = new String[2];
        tokenArray[0] = token.replace("Bearer ", "");
        tokenArray[1] = refreshToken;
        for (int i = 0; i < tokenArray.length; i++) {
            String realToken = tokenArray[i];
            JWT jwtObj = JWTUtil.parseToken(realToken);

            String jti = jwtObj.getPayload("jti").toString();
            Long exp = Long.parseLong(jwtObj.getPayload("exp").toString());
            if (exp - currentTimeSeconds > 0) {
                redisUtil.setForTimeCustom("JWTBLOCK:"+jti, jti, (exp - currentTimeSeconds), TimeUnit.SECONDS);
            }
        }
        return new Response().success();
    }
}
