package io.nerv.exception;

import io.nerv.core.mvc.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class LoginExceptionAdvice {

    /**
     * 401 - 权限不足
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(LoginException.class)
    public Response handleLoginException(LoginException e){
        log.error("登录失败.:" + e.getMessage());
        return new Response().failure(401, e.getMessage());
    }
}
