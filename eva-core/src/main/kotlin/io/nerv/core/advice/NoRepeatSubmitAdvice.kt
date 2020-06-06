package io.nerv.core.advice

import cn.hutool.crypto.SecureUtil
import io.nerv.core.constant.CommonConstant
import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.token.util.TokenUtil
import io.nerv.exception.BizException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * 防止重复提交切入点
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "eva", name = ["norepeat-check"], havingValue = "true")
class NoRepeatSubmitAdvice {
    var log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private val cacheManager: CacheManager? = null

    @Autowired
    private val tokenUtil: TokenUtil? = null

    @Around("@annotation(io.nerv.core.annotation.NoRepeatSubmit)")
    fun arround(pjp: ProceedingJoinPoint): Any {
        val cache = cacheManager!!.getCache(CommonConstant.CACHE_REPEATSUBMIT)
        return try {
            val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
            val request = attributes.request
            // 请求类型
            val method = request.method
            var key: String? = method + ":" + tokenUtil!!.getToken(request) + "-" + request.servletPath
            key = SecureUtil.md5(key)

            // 如果缓存中有这个url视为重复提交
            if (cache[key] == null) {
                cache.put(key, 0)
            } else {
                throw BizException(BizCodeEnum.REQUEST_TOO_MORE)
            }
            pjp.proceed()
        } catch (e: Throwable) {
            log.error("验证重复提交时出现未知异常!", e)
            throw BizException(BizCodeEnum.REQUEST_TOO_MORE)
        }
    }
}