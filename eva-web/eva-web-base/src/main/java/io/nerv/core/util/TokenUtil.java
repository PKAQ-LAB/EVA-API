package io.nerv.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class TokenUtil {
    @Autowired
    private EvaConfig evaConfig;

    /**
     * 获取access token
     * @param request
     * @return
     */
    public String getToken(HttpServletRequest request){
        return this.getToken(request, CommonConstant.ACCESS_TOKEN_KEY);
    }
    /**
     * 获取refresh token
     * @param request
     * @return
     */
    public String getRefreshToken(HttpServletRequest request){
        return this.getToken(request, CommonConstant.REFRESH_TOKEN_KEY);
    }
    /**
     * 获取token
     * @param request
     * @return
     */
    public String getToken(HttpServletRequest request, String tokenKey){
        String authToken = null;

        var authHeader = request.getHeader(evaConfig.getJwt().getHeader());

        if(null != ServletUtil.getCookie(request, CommonConstant.ACCESS_TOKEN_KEY)){
            authToken = ServletUtil.getCookie(request, CommonConstant.ACCESS_TOKEN_KEY).getValue();
        } else  if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(evaConfig.getJwt().getTokenHead())) {
            authToken = authHeader.substring(evaConfig.getJwt().getTokenHead().length());
        }

        return authToken;
    }
}
