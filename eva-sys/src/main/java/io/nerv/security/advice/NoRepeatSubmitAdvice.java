package io.nerv.security.advice;

import cn.hutool.crypto.SecureUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.security.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
    private CacheManager cacheManager;

    @Autowired
    private TokenUtil tokenUtil;

    @Around("@annotation(io.nerv.core.annotation.NoRepeatSubmit)")
    public Object arround(ProceedingJoinPoint pjp) {
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_REPEATSUBMIT);

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            var key = tokenUtil.getToken(request) + "-" + request.getServletPath();
            key = SecureUtil.md5(key);

            // 如果缓存中有这个url视为重复提交
            if (cache.get(key) == null) {
                Object o = pjp.proceed();
                cache.put(key, 0);
                return o;
            } else {
                throw new BizException(BizCodeEnum.REQUEST_TOO_MORE);
            }
        } catch (Throwable e) {
            log.error("验证重复提交时出现未知异常!", e);
            throw new BizException(BizCodeEnum.REQUEST_TOO_MORE);
        }

    }
}
