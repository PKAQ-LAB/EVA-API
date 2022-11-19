package io.nerv.core.web.advice;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.util.I18NHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * @Description:      统一异常处理
 * @FileName:         ExceptionAdvice.java
 * @Author:           S.PKAQ
 * @Version:          1.0
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionAdvice {
    private final I18NHelper i18NHelper;

    /**
     * hibernate validator参数校验失败时抛出的异常
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Response handleViolationException(ConstraintViolationException e){
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder message = new StringBuilder();

        for (ConstraintViolation<?> item : violations) {
            message.append(item.getMessage());
        }
        return new Response().failure(BizCodeEnum.PARAM_TYPEERROR.getCode(), message.toString());
    }

    /**
     * hibernate validator参数校验失败时抛出的异常
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleMethodParamCheckException(MethodArgumentNotValidException e){
        return new Response().failure(BizCodeEnum.PARAM_TYPEERROR.getCode(),  e.getBindingResult().getFieldError().getDefaultMessage());
    }
    /**
     * 业务异常
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BizException.class)
    public Response handleBindException(BizException e) {
        log.error("业务异常:"+e.getMessage());
        e.printStackTrace();
        return i18NHelper.getMessage(e.getBizCode());
    }

    /**
     * 400异常.- 参数错误
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("参数解析失败："+e.getMessage());
        return i18NHelper.getMessage(BizCodeEnum.PARAM_TYPEERROR);
    }
    /**
     *  405 - Method Not Allowed.
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持当前请求方法:"+e.getMessage());
        return i18NHelper.getMessage(BizCodeEnum.REQUEST_METHOD_ERROR);
    }
    /**
     *  415 - Unsupported Media Type.
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Response handleHttpMediaTypeNotSupportedException(Exception e) {
        log.warn("不支持当前媒体类型:"+e.getMessage());
        return i18NHelper.getMessage(BizCodeEnum.REQUEST_MEDIA_ERROR);
    }
    /**
     * 参数类型错误
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    public Response handleIllegalArgumentException(Exception e) {
        log.warn("参数类型错误：不支持当前请求的参数类型:"+e.getMessage());
        return i18NHelper.getMessage(BizCodeEnum.PARAM_TYPEERROR);
    }
    /**
     *   400 - spring参数绑定校验错误
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Response handleBindException(BindException e) {
        log.error("服务运行异常:"+e.getMessage());
        e.printStackTrace();
        StringBuilder errorMsg = new StringBuilder();
        e.getAllErrors().forEach(
                x -> errorMsg.append(x.getDefaultMessage()).append(",")
        );
        return new Response().failure(BizCodeEnum.SERVER_ERROR.getCode(), errorMsg.toString());
    }

    /**
     *   500 - Internal Server Error.
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        log.error("服务运行异常:"+e.getMessage());
        e.printStackTrace();
        return i18NHelper.getMessage(BizCodeEnum.SERVER_ERROR);
    }
}
