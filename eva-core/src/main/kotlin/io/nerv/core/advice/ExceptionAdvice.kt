package io.nerv.core.advice

import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.mvc.util.Response
import io.nerv.exception.BizException
import io.nerv.exception.OathException
import io.nerv.exception.ParamException
import io.nerv.exception.ReflectException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.ObjectError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.function.Consumer
import javax.validation.ConstraintViolationException

/**
 * @Description:      统一异常处理
 * @FileName:         ExceptionAdvice.java
 * @Author:           S.PKAQ
 * @Version:          1.0
 */
@RestControllerAdvice
class ExceptionAdvice {
    var log = LoggerFactory.getLogger(this.javaClass)
    /**
     * hibernate validator参数校验失败时抛出的异常
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleViolationException(e: ConstraintViolationException): Response {
        val violations = e.constraintViolations
        val message = StringBuilder()
        for (item in violations) {
            message.append(item.message)
        }
        return Response().failure(BizCodeEnum.PARAM_TYPEERROR.getIndex(), message.toString())
    }

    /**
     * hibernate validator参数校验失败时抛出的异常
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodParamCheckException(e: MethodArgumentNotValidException): Response {
        return Response().failure(BizCodeEnum.PARAM_TYPEERROR.getIndex(), e.bindingResult.fieldError.defaultMessage)
    }

    /**
     * 400 - 捕获自定义参数异常
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ParamException::class)
    fun handleException(e: ParamException): Response {
        return Response().failure(BizCodeEnum.PARAM_ERROR.getIndex(), e.msg)
    }

    /**
     * 400异常.- 参数错误
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): Response {
        log.error("参数解析失败：" + e.message)
        return Response().failure(BizCodeEnum.PARAM_TYPEERROR)
    }

    /**
     * 405 - Method Not Allowed.
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): Response {
        log.warn("不支持当前请求方法:" + e.message)
        return Response().failure(BizCodeEnum.REQUEST_METHOD_ERROR)
    }

    /**
     * 415 - Unsupported Media Type.
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(e: Exception): Response {
        log.warn("不支持当前媒体类型:" + e.message)
        return Response().failure(BizCodeEnum.REQUEST_MEDIA_ERROR)
    }

    /**
     * 参数类型错误
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class, MissingServletRequestParameterException::class)
    fun handleIllegalArgumentException(e: Exception): Response {
        log.warn("参数类型错误：不支持当前请求的参数类型:" + e.message)
        return Response().failure(BizCodeEnum.PARAM_TYPEERROR)
    }

    /**
     * 400 - spring参数绑定校验错误
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): Response {
        log.error("服务运行异常:" + e.message)
        e.printStackTrace()
        val errorMsg = StringBuilder()
        e.allErrors.forEach(
                Consumer { x: ObjectError -> errorMsg.append(x.defaultMessage).append(",") }
        )
        return Response().failure(BizCodeEnum.SERVER_ERROR.getIndex(), errorMsg.toString())
    }

    /**
     * 业务异常
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BizException::class)
    fun handleBindException(e: BizException): Response {
        log.error("业务异常:" + e.message)
        e.printStackTrace()
        return Response().failure(e.code, e.message)
    }

    /**
     * 鉴权异常
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(OathException::class)
    fun handleBindException(e: OathException): Response {
        log.error("业务异常:" + e.message)
        e.printStackTrace()
        return Response().failure(e.code, e.message)
    }

    /**
     * 500 - Internal Server Error.
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): Response {
        log.error("服务运行异常:" + e.message)
        e.printStackTrace()
        return Response().failure(BizCodeEnum.SERVER_ERROR)
    }

    /**
     * 500 - Internal Server Error.
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ReflectException::class)
    fun reflectException(e: ReflectException): Response {
        log.error("反射异常:" + e.message)
        e.printStackTrace()
        return Response().failure(BizCodeEnum.SERVER_ERROR)
    }
}