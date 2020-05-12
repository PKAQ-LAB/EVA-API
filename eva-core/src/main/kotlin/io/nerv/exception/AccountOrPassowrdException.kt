package io.nerv.exception

import org.springframework.security.core.AuthenticationException

/**
 * 自定义鉴权异常类
 */
class AccountOrPassowrdException(msg: String?) : AuthenticationException(msg) 