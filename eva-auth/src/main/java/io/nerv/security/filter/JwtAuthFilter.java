package io.nerv.security.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.OathException;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.token.jwt.JwtUtil;
import io.nerv.core.token.util.TokenUtil;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("jwtUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        var isvalid = false;
        var inCache = false;

        var authToken = tokenUtil.getToken(request);

        if(null != ServletUtil.getCookie(request, CommonConstant.TOKEN_KEY)){
            authToken = ServletUtil.getCookie(request, CommonConstant.TOKEN_KEY).getValue();
        }

        if (StrUtil.isNotBlank(authToken)) {
            String uid = jwtUtil.getUid(authToken);

            /**
             * token即将过期 刷新
             */
            try {
                // 验证token 是否合法
                isvalid = jwtUtil.valid(authToken);
                // 验证缓存中是否存在该token
                Object token = tokenUtil.getToken(uid);

                JSONObject jsonObject = null;
                if (null != token){
                    jsonObject = JSON.parseObject(String.valueOf(token));
                }

                if ( null != jsonObject &&
                    authToken.equals(jsonObject.getString(CommonConstant.CACHE_TOKEN))) {
                    inCache = true;
                } else {
                    logger.warn("鉴权失败 缓存中无法找到对应token");
                    // 清除cookie
                    ServletUtil.addCookie(response,
                            CommonConstant.TOKEN_KEY,
                            null,
                            0,
                            "/",
                            evaConfig.getCookie().getDomain());

                    throw new OathException(BizCodeEnum.LOGIN_EXPIRED);
                }

                // token 即将过期 续命
                if (jwtUtil.isTokenExpiring(authToken)){
                    String newToken = jwtUtil.refreshToken(authToken);
                    // 刷新缓存中的
                    // token放入缓存
                    tokenUtil.saveToken(uid, tokenUtil.buildCacheValue(request, uid, newToken));

                    // reponse请求头返回刷新后的token
                    response.setHeader(CommonConstant.TOKEN_KEY, newToken);
                    // 后台设置前台cookie值
                    ServletUtil.addCookie(response, CommonConstant.TOKEN_KEY,
                                                    newToken,
                                                    evaConfig.getCookie().getMaxAge(),
                                                "/",
                                                    evaConfig.getCookie().getDomain());
                }
            } catch (OathException e) {
                logger.warn("鉴权失败 Token已过期", e);

                // 清除cookie
                ServletUtil.addCookie(response,
                        CommonConstant.TOKEN_KEY,
                        null,
                        0,
                        "/",
                        evaConfig.getCookie().getDomain());

                try(PrintWriter printWriter = response.getWriter()){
                    printWriter.write(JSON.toJSONString(
                            new Response()
                                    .failure(BizCodeEnum.LOGIN_EXPIRED)
                            )
                    );
                    printWriter.flush();
                }
                return;
            }
        } else {
            logger.warn("couldn't find bearer string, will ignore the header");
        }

        if (isvalid && inCache) {
            String uid = jwtUtil.getUid(authToken);

            logger.info("checking authentication ：" + uid);

            if (StrUtil.isNotBlank(uid) && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("io.nerv.security context was null, so authorizing user");

                // 从redis中 根据用户id获取用户权限列表
                UserDetails userDetails;
                try{
                    userDetails = this.userDetailsService.loadUserByUsername(uid);
                } catch(UsernameNotFoundException e){
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "您的登录已过期, 请重新登录.");
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                logger.info("authenticated user " + uid + ", setting io.nerv.security context");

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}