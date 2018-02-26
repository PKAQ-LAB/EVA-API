package org.pkaq.web.account.entity

import java.io.Serializable
import java.util.Date

/**
 * 用户账户信息
 * @author PKAQ
 */
class UserEntity : Serializable {
    var id: String? = null

    var code: String? = null

    var account: String? = null

    var password: String? = null

    var salt: String? = null

    var avatar: String? = null

    var name: String? = null

    var nickName: String? = null

    var registerIp: String? = null

    var gmtRegister: Date? = null

    var lastIp: String? = null

    var lastLogin: Date? = null

    var locked: Byte? = null

    var gmtCreate: Date? = null

    var gmtModify: Date? = null

    var createBy: String? = null

    var modifyBy: String? = null

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(javaClass.simpleName)
        sb.append(" [")
        sb.append("Hash = ").append(hashCode())
        sb.append(", id=").append(id)
        sb.append(", code=").append(code)
        sb.append(", account=").append(account)
        sb.append(", password=").append(password)
        sb.append(", salt=").append(salt)
        sb.append(", avatar=").append(avatar)
        sb.append(", name=").append(name)
        sb.append(", nickName=").append(nickName)
        sb.append(", registerIp=").append(registerIp)
        sb.append(", gmtRegister=").append(gmtRegister)
        sb.append(", lastIp=").append(lastIp)
        sb.append(", lastLogin=").append(lastLogin)
        sb.append(", locked=").append(locked)
        sb.append(", gmtCreate=").append(gmtCreate)
        sb.append(", gmtModify=").append(gmtModify)
        sb.append(", createBy=").append(createBy)
        sb.append(", modifyBy=").append(modifyBy)
        sb.append(", serialVersionUID=").append(serialVersionUID)
        sb.append("]")
        return sb.toString()
    }

    override fun equals(that: Any?): Boolean {
        if (this === that) {
            return true
        }
        if (that == null) {
            return false
        }
        if (javaClass != that.javaClass) {
            return false
        }
        val other = that as UserEntity?
        return ((if (this.id == null) other!!.id == null else this.id == other!!.id)
                && (if (this.code == null) other.code == null else this.code == other.code)
                && (if (this.account == null) other.account == null else this.account == other.account)
                && (if (this.password == null) other.password == null else this.password == other.password)
                && (if (this.salt == null) other.salt == null else this.salt == other.salt)
                && (if (this.avatar == null) other.avatar == null else this.avatar == other.avatar)
                && (if (this.name == null) other.name == null else this.name == other.name)
                && (if (this.nickName == null) other.nickName == null else this.nickName == other.nickName)
                && (if (this.registerIp == null) other.registerIp == null else this.registerIp == other.registerIp)
                && (if (this.gmtRegister == null) other.gmtRegister == null else this.gmtRegister == other.gmtRegister)
                && (if (this.lastIp == null) other.lastIp == null else this.lastIp == other.lastIp)
                && (if (this.lastLogin == null) other.lastLogin == null else this.lastLogin == other.lastLogin)
                && (if (this.locked == null) other.locked == null else this.locked == other.locked)
                && (if (this.gmtCreate == null) other.gmtCreate == null else this.gmtCreate == other.gmtCreate)
                && (if (this.gmtModify == null) other.gmtModify == null else this.gmtModify == other.gmtModify)
                && (if (this.createBy == null) other.createBy == null else this.createBy == other.createBy)
                && if (this.modifyBy == null) other.modifyBy == null else this.modifyBy == other.modifyBy)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (id == null) 0 else id!!.hashCode()
        result = prime * result + if (code == null) 0 else code!!.hashCode()
        result = prime * result + if (account == null) 0 else account!!.hashCode()
        result = prime * result + if (password == null) 0 else password!!.hashCode()
        result = prime * result + if (salt == null) 0 else salt!!.hashCode()
        result = prime * result + if (avatar == null) 0 else avatar!!.hashCode()
        result = prime * result + if (name == null) 0 else name!!.hashCode()
        result = prime * result + if (nickName == null) 0 else nickName!!.hashCode()
        result = prime * result + if (registerIp == null) 0 else registerIp!!.hashCode()
        result = prime * result + if (gmtRegister == null) 0 else gmtRegister!!.hashCode()
        result = prime * result + if (lastIp == null) 0 else lastIp!!.hashCode()
        result = prime * result + if (lastLogin == null) 0 else lastLogin!!.hashCode()
        result = prime * result + if (locked == null) 0 else locked!!.hashCode()
        result = prime * result + if (gmtCreate == null) 0 else gmtCreate!!.hashCode()
        result = prime * result + if (gmtModify == null) 0 else gmtModify!!.hashCode()
        result = prime * result + if (createBy == null) 0 else createBy!!.hashCode()
        result = prime * result + if (modifyBy == null) 0 else modifyBy!!.hashCode()
        return result
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}