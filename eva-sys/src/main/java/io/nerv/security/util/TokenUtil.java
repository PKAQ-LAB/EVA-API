package io.nerv.security.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import io.nerv.core.constant.TokenConst;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class TokenUtil {
    @Autowired
    private EvaConfig evaConfig;

    /**
     * 获取基线代码
     * @param request
     * @return
     */
    public String getToken(HttpServletRequest request){
        String authToken = null;

        var authHeader = request.getHeader(evaConfig.getJwt().getHeader());

        if(null != ServletUtil.getCookie(request, TokenConst.TOKEN_KEY)){
            authToken = ServletUtil.getCookie(request, TokenConst.TOKEN_KEY).getValue();
        } else  if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(evaConfig.getJwt().getTokenHead())) {
            authToken = authHeader.substring(evaConfig.getJwt().getTokenHead().length());
        }

        return authToken;
    }
}
