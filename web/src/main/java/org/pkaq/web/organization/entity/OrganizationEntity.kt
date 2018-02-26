package org.pkaq.web.organization.entity

import java.io.Serializable
import java.util.Date

class OrganizationEntity : Serializable {
    var id: String? = null

    var name: String? = null

    var code: String? = null

    var parentid: String? = null

    var parentname: String? = null

    var path: String? = null

    var pathname: String? = null

    var isleaf: Byte? = null

    var orders: Int? = null

    var status: String? = null

    var remark: String? = null

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
        sb.append(", name=").append(name)
        sb.append(", code=").append(code)
        sb.append(", parentid=").append(parentid)
        sb.append(", parentname=").append(parentname)
        sb.append(", path=").append(path)
        sb.append(", pathname=").append(pathname)
        sb.append(", isleaf=").append(isleaf)
        sb.append(", orders=").append(orders)
        sb.append(", status=").append(status)
        sb.append(", remark=").append(remark)
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
        val other = that as OrganizationEntity?
        return ((if (this.id == null) other!!.id == null else this.id == other!!.id)
                && (if (this.name == null) other.name == null else this.name == other.name)
                && (if (this.code == null) other.code == null else this.code == other.code)
                && (if (this.parentid == null) other.parentid == null else this.parentid == other.parentid)
                && (if (this.parentname == null) other.parentname == null else this.parentname == other.parentname)
                && (if (this.path == null) other.path == null else this.path == other.path)
                && (if (this.pathname == null) other.pathname == null else this.pathname == other.pathname)
                && (if (this.isleaf == null) other.isleaf == null else this.isleaf == other.isleaf)
                && (if (this.orders == null) other.orders == null else this.orders == other.orders)
                && (if (this.status == null) other.status == null else this.status == other.status)
                && (if (this.remark == null) other.remark == null else this.remark == other.remark)
                && (if (this.gmtCreate == null) other.gmtCreate == null else this.gmtCreate == other.gmtCreate)
                && (if (this.gmtModify == null) other.gmtModify == null else this.gmtModify == other.gmtModify)
                && (if (this.createBy == null) other.createBy == null else this.createBy == other.createBy)
                && if (this.modifyBy == null) other.modifyBy == null else this.modifyBy == other.modifyBy)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (id == null) 0 else id!!.hashCode()
        result = prime * result + if (name == null) 0 else name!!.hashCode()
        result = prime * result + if (code == null) 0 else code!!.hashCode()
        result = prime * result + if (parentid == null) 0 else parentid!!.hashCode()
        result = prime * result + if (parentname == null) 0 else parentname!!.hashCode()
        result = prime * result + if (path == null) 0 else path!!.hashCode()
        result = prime * result + if (pathname == null) 0 else pathname!!.hashCode()
        result = prime * result + if (isleaf == null) 0 else isleaf!!.hashCode()
        result = prime * result + if (orders == null) 0 else orders!!.hashCode()
        result = prime * result + if (status == null) 0 else status!!.hashCode()
        result = prime * result + if (remark == null) 0 else remark!!.hashCode()
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