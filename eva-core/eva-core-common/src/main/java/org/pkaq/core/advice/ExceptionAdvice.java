package org.pkaq.core.advice;

import cn.hutool.core.util.ObjectUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.vo.Response;
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

import java.util.Set;
/**
 * @Description: 统一异常处理
 * @FileName: ExceptionAdvice.java
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionAdvice {
    /**
     * hibernate validator参数校验失败时抛出的异常
     // 处理方法参数上的 @Validated（如 service 层方法）
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Object> handleViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder message = new StringBuilder();

        for (ConstraintViolation<?> item : violations) {
            message.append(item.getMessage());
        }
        return Response.failure(CommonCodes.PARAM_TYPEERROR.getCode(), message.toString());
    }

    /**
     * hibernate validator参数校验失败时抛出的异常
     // 处理 @Valid + @RequestBody 参数校验异常
     * @param e
     * @return
     */

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Object> handleMethodParamCheckException(MethodArgumentNotValidException e) {
        return Response.failure(CommonCodes.PARAM_TYPEERROR.getCode(), e.getBindingResult().getFieldError().getDefaultMessage());
    }

    /**
     * 400异常.- 参数错误
     *
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("参数解析失败：" + e.getMessage());
        return Response.failure(CommonCodes.PARAM_TYPEERROR);
    }

    /**
     * 参数类型错误
     // 缺少参数
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    public Response<Object> handleIllegalArgumentException(Exception e) {
        log.warn("参数类型错误：不支持当前请求的参数类型:" + e.getMessage());
        return Response.failure(CommonCodes.PARAM_TYPEERROR);
    }

    /**
     * 400 - spring参数绑定校验错误
     // 处理 @Valid + 表单对象（@ModelAttribute）验证异常
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Response<Object> handleBindException(BindException e) {
        log.error("服务运行异常:" + e.getMessage());
        StringBuilder errorMsg = new StringBuilder();
        e.getAllErrors().forEach(
                x -> errorMsg.append(x.getDefaultMessage()).append(",")
        );
        return Response.failure(CommonCodes.SERVER_ERROR.getCode(), errorMsg.toString());
    }

    /**
     * 业务异常
     *
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BizException.class)
    public Response<Object> handleBindException(BizException e) {
        String msg = ObjectUtil.defaultIfBlank(e.getMessage(), e.getEstr());

        log.error("业务异常:" + msg);

        if (null == e.getBizCode()) {
            return Response.failure(null, e.getMessage(), e.getData(),e.getArgs());
        } else {
            return Response.failure(e.getBizCode(), e.getData(), e.getArgs());
        }
    }

    /**
     * 405 - Method Not Allowed.
     *
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持当前请求方法:" + e.getMessage());
        return Response.failure(CommonCodes.REQUEST_METHOD_ERROR);
    }

    /**
     * 415 - Unsupported Media Type.
     *
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Response<Object> handleHttpMediaTypeNotSupportedException(Exception e) {
        log.warn("不支持当前媒体类型:" + e.getMessage());
        return Response.failure(CommonCodes.REQUEST_MEDIA_ERROR);
    }

    /**
     * 500 - Internal Server Error.
     *
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Response<Object> handleException(Exception e) {
        log.error("服务运行异常:" + e.getMessage());
        return Response.failure(CommonCodes.SERVER_ERROR);
    }
}
