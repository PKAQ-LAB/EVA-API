package io.nerv.security.advice;

import cn.hutool.cache.Cache;
import cn.hutool.core.net.NetUtil;
import cn.hutool.crypto.SecureUtil;
import io.nerv.security.jwt.JwtUtil;
import io.nerv.security.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 防止重复提交切入点
 */
@Aspect
@Component
@Slf4j
public class NoRepeatSubmitAdvice {
    @Autowired
    @Qualifier("fifoCache")
    private Cache<String, Integer> cache;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Around("@annotation(io.nerv.core.annotation.NoRepeatSubmit)")
    public Object arround(ProceedingJoinPoint pjp) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            var key = tokenUtil.getToken(request) + "-" + request.getServletPath();
            key = SecureUtil.md5(key);

            if (cache.get(key) == null) {// 如果缓存中有这个url视为重复提交
                Object o = pjp.proceed();
                cache.put(key, 0);
                return o;
            } else {
                log.error("重复提交");
                return null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            log.error("验证重复提交时出现未知异常!");
            return "{\"code\":-889,\"message\":\"验证重复提交时出现未知异常!\"}";
        }

    }
}
