package org.pkaq.core.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.StrUtils;
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
 * 统一异常处理
 * @author PKAQ
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionAdvice {
    /**
     * hibernate validator参数校验失败时抛出的异常
     * // 处理方法参数上的 @Validated（如 service 层方法）
     *
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Object> handleViolationException(ConstraintViolationException e) {
        log.error("参数校验失败：" + e.getMessage());

        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder message = new StringBuilder();

        for (ConstraintViolation<?> item : violations) {
            message.append(item.getMessage());
        }
        return Response.failure(CommonCodes.PARAM_TYPE_ERROR.getCode(), message.toString());
    }

    /**
     * hibernate validator参数校验失败时抛出的异常
     * // 处理 @Valid + @RequestBody 参数校验异常
     *
     */

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Object> handleMethodParamCheckException(MethodArgumentNotValidException e) {

        log.error("参数校验失败：" + e.getMessage());

        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

        return Response.failure(CommonCodes.PARAM_TYPE_ERROR.getCode(), e.getBindingResult().getFieldError().getDefaultMessage());
    }

    /**
     * 400 - spring参数绑定校验错误
     * // 处理 @Valid + 表单对象（@ModelAttribute）验证异常
     *
     * @param e 异常类型
     * @return Response
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Response<Object> handleBindException(BindException e) {
        log.error("服务运行异常:" + e.getMessage());

        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

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
        String msg = StrUtils.defaultIfBlank(e.getMessage(), e.getEstr());
        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

        log.error("业务异常:" + msg);

        if (null == e.getBizCode()) {
            return Response.failure(null, e.getMessage(), e.getData(), e.getArgs());
        } else {
            return Response.failure(e.getBizCode(), e.getData(), e.getArgs());
        }
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
        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }

        return Response.failure(CommonCodes.SERVER_ERROR);
    }
}
