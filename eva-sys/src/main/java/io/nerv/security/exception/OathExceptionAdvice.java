package io.nerv.security.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.nerv.core.mvc.util.Response;
import io.nerv.exception.OathException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @Description:      统一异常处理
 * @FileName:         ExceptionAdvice.java
 * @Author:           S.PKAQ
 * @Version:          1.0
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class OathExceptionAdvice {
    /**
     * 403 - 访问被拒绝
     * 权限不足异常
     * @param e 异常类型
     * @return
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Response handleAccessDeniedException(AccessDeniedException e){
        log.error("没有操作权限:" + e.getMessage());
        return new Response().failure(403);
    }

    /**
     * 401 - 权限不足
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExpiredJwtException.class)
    public Response handleAccessDeniedException(ExpiredJwtException e){
        log.error("Token 已过期, 请重新登录.:" + e.getMessage());
        return new Response().failure(401, "您的登录已过期, 请重新登录.");
    }

    /**
     * 401 - 权限不足
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(OathException.class)
    public Response handleAccessDeniedException(OathException e){
        log.error("权限不足.:" + e.getMessage());
        return new Response().failure(401, e.getMessage());
    }
}
