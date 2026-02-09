package org.pkaq.web.core.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Description: 统一异常处理
 * @FileName: ExceptionAdvice.java
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class HttpExceptionAdvice {

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

        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

        return Response.failure(CommonCodes.PARAM_TYPE_ERROR);
    }

    /**
     * 参数类型错误
     * // 缺少参数
     *
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    public Response<Object> handleIllegalArgumentException(Exception e) {
        log.warn("参数类型错误：不支持当前请求的参数类型:" + e.getMessage());

        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

        return Response.failure(CommonCodes.PARAM_TYPE_ERROR);
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

        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

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

        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

        return Response.failure(CommonCodes.REQUEST_MEDIA_ERROR);
    }
}
