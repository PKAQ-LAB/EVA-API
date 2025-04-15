package org.pkaq.core.web.advice;

import cn.hutool.crypto.SecureUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.web.util.TokenUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 防止重复提交切入点
 *
 * @author PKAQ
 */
@Aspect
@Component
@Slf4j

@ConditionalOnProperty(prefix = "eva", name = "norepeat-check", havingValue = "true")
@RequiredArgsConstructor
public class NoRepeatSubmitAdvice {

    private final CacheManager cacheManager;

    private final TokenUtil tokenUtil;

    @Around("@annotation(org.pkaq.core.annotation.NoRepeatSubmit)")
    public Object arround(ProceedingJoinPoint pjp) {
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_REPEATSUBMIT);

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            // 请求类型
            String method = request.getMethod();
            var key = method + ":" + tokenUtil.getToken(request) + "-" + request.getServletPath();
            key = SecureUtil.md5(key);


            // 如果缓存中有这个url视为重复提交
            if (cache.get(key) == null) {
                cache.put(key, 0);
            } else {
                throw new BizException(BizCodeEnum.REQUEST_TOO_MORE);
            }
            return pjp.proceed();
        } catch (Throwable e) {
            log.error("验证重复提交时出现未知异常!", e);
            throw new BizException(BizCodeEnum.REQUEST_TOO_MORE);
        }

    }
}
