package io.nerv.core.log

import cn.hutool.core.date.DateUtil
import cn.hutool.core.exceptions.ExceptionUtil
import io.nerv.core.util.IpUtil
import io.nerv.exception.entity.ErrorlogEntity
import io.nerv.exception.mapper.ErrorlogMapper
import io.nerv.properties.EvaConfig
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.*
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * AOP记录web日志
 * @author PKAQ
 */
@Aspect
@Component
class WebLogAdvice {
    var log: Logger = LoggerFactory.getLogger(this.javaClass)

    private val startTime = ThreadLocal<Long>()

    @Autowired
    private val errorlogMapper: ErrorlogMapper? = null

    @Autowired
    private val evaConfig: EvaConfig? = null

    /**
     * 定义一个切入点.
     * ~ 第一个 * 代表任意修饰符及任意返回值.
     * ~ 第二个 * 任意包名
     * ~ 第三个 * 代表任意方法.
     * ~ 第四个 * 定义在web包或者子包
     * ~ 第五个 * 任意方法
     * ~ .. 匹配任意数量的参数.
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)" +
            "@within(org.springframework.stereotype.Controller)")
    fun webLog() {
    }

    @Before("webLog()")
    fun doBefore(joinPoint: JoinPoint) {

        //记录开始时间
        startTime.set(System.currentTimeMillis())

        // 接收到请求，记录请求内容
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        if (null != attributes) {
            val request = attributes.request
            // 记录下请求内容
            log.info("---------------------------start---------------------------")
            log.info("URL : " + request.requestURL.toString())
            log.info("Header: " + request.headerNames)
            log.info("Device: " + request.getHeader("device"))
            log.info("Version: " + request.getHeader("version"))
            log.info("HTTP_METHOD : " + request.method)
            log.info("IP : " + request.remoteAddr)
            log.info("CLASS_METHOD : " + joinPoint.signature.declaringTypeName + "." + joinPoint.signature.name)
            log.info("ARGS : " + Arrays.toString(joinPoint.args))
            log.info("---------------------------end---------------------------")
        }
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    fun doAfterReturning(joinPoint: JoinPoint, ret: Any) {
        val signature = joinPoint.signature as MethodSignature
        // 处理完请求，返回内容
        log.info("RESPONSE : $ret")
        log.info("SPEND TIME : " + (System.currentTimeMillis() - startTime.get()))
    }

    /**
     * 记录异常日志
     * @param joinPoint
     * @param ex
     */
    @AfterThrowing(pointcut = "webLog()", throwing = "ex")
    fun doWhenThrowing(joinPoint: JoinPoint, ex: Throwable?) {
        val jsontStack = ExceptionUtil.stacktraceToString(ex)
        log.error(jsontStack)
        if (!evaConfig!!.errorLog!!.enabled) return
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        if (null != attributes) {
            val request = attributes.request
            val ip = IpUtil.getIPAddress(request)
            val className = joinPoint.target.javaClass.name
            val methodName = joinPoint.signature.name
            val args = joinPoint.args
            var argStr: String? = ""
            for (arg in args) {
                if (arg is MultipartFile) continue
                argStr += arg
            }
            val errorlogEntity = ErrorlogEntity()
            errorlogEntity.requestTime = DateUtil.now()
            errorlogEntity.className = className
            errorlogEntity.method = methodName
            errorlogEntity.params = argStr
            errorlogEntity.exDesc = jsontStack
            errorlogEntity.ip = ip
            errorlogEntity.spendTime = (System.currentTimeMillis() - startTime.get()).toString()
            errorlogMapper!!.insert(errorlogEntity)
        }
    }
}