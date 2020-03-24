package io.nerv.security.entrypoint;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.util.RequestUtil;
import io.nerv.core.util.SecurityHelper;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义注销成功处理器
 */
@Component
public class UrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private RequestUtil requestUtil;

    @Autowired
    private SecurityHelper securityHelper;

    private Cache tokenCache;

    public UrlLogoutSuccessHandler(CacheManager cacheManager) {
        // 获取存放token的cache
        this.tokenCache = cacheManager.getCache(CommonConstant.CACHE_TOKEN);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException {

        String cacheKey = String.format("%s::%s", securityHelper.getJwtUser().getAccount(), requestUtil.formatDeivceAndVersion(httpServletRequest, "%s::%s"));


        // 清空redis/caffeine中的token 刷新用户secret
        this.tokenCache.evict(cacheKey);

        // 清除cookie
        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.TOKEN_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());


        ServletUtil.addCookie(httpServletResponse,
                CommonConstant.USER_KEY,
                null,
                0,
                "/",
                evaConfig.getCookie().getDomain());

        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        httpServletResponse.getWriter().write(JSON.toJSONString(new Response().failure(BizCodeEnum.LOGINOUT_SUCCESS)));
    }
}
