package io.nerv.core.util

import io.nerv.core.security.domain.JwtUserDetail
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

/**
 * Spring security 工具类
 */
@Slf4j
@Component
class SecurityHelper {
    /**
     * 判断是否是管理员
     * @return
     */
    val isAdmin: Boolean
        get() = authentication
                .authorities
                .stream()
                .map { obj: GrantedAuthority -> obj.authority }
                .anyMatch { str: String -> str == "ROLE_ADMIN" }

    /**
     * 获取当前用户的权限数组
     * @return
     */
    val roleNames: Array<String>
        get() = authentication
                .authorities
                .stream()
                .map { obj: GrantedAuthority -> obj.authority }
                .toArray { _Dummy_.__Array__() }

    /**
     * 获取权限对象
     * @return
     */
    val authentication: Authentication
        get() = SecurityContextHolder.getContext().authentication

    /**
     * 获取用户名
     * @param request
     * @return
     */
    fun getUser(request: HttpServletRequest): String {
        return request.userPrincipal.name
    }

    /**
     * 获取当前登录用户对象
     * @return
     */
    @get:Throws(ClassCastException::class)
    val jwtUser: JwtUserDetail
        get() = authentication.principal as JwtUserDetail

    /**
     * 获取用户ID
     * @return
     */
    val jwtUserId: String
        get() {
            var userId = "Anonymous"
            try {
                userId = jwtUser.id
            } catch (e: Exception) {
                SecurityHelper.log.error("获取用户ID错误： " + e.message)
            } finally {
                return userId
            }
        }

    /**
     * 获取用户名
     * @return
     */
    val jwtUserName: String
        get() {
            var userName = "Anonymous"
            try {
                userName = jwtUser.account
            } catch (e: Exception) {
                e.printStackTrace()
                SecurityHelper.log.error("获取用户名错误： " + e.message)
            } finally {
                return userName
            }
        }
}