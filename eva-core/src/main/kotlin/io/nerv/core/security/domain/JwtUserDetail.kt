package io.nerv.core.security.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.Data
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * JwtUser
 * @author: S.PKAQ
 * @Datetime: 2018/4/24 23:14
 */
open class JwtUserDetail(
        /**用户ID */
    var id: String,
        /**用户账号 */
    var account: String,
        /**密码 */
    @JsonIgnore
    private var password: String,
        /**部门id */
    var deptId: String,
        /**部门名称 */
    var deptName: String,
        /**用户姓名 */
    var name: String,
        /**用户昵称 */
    var nickName: String, accountNonLocked: Boolean, authorities: Collection<GrantedAuthority?>) : UserDetails {

    /**用户是否已经锁定 */
    var accountNonLocked: Boolean

    /**权限集合 */
    var authorities: Collection<GrantedAuthority?>

    override fun getUsername(): String {
        return account
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getPassword(): String {
        TODO("Not yet implemented")
    }

    override fun isEnabled(): Boolean {
        return true
    }

    init {
        this.accountNonLocked = !accountNonLocked
        this.authorities = authorities
    }
}