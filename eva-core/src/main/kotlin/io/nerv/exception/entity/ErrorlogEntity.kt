package io.nerv.exception.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.Data
import lombok.experimental.Accessors
import org.apache.ibatis.type.Alias

/**
 * 异常日志实体类
 */
@Alias("errorlog")
@TableName("log_error")
class ErrorlogEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null

    /** 操作人  */
    var requestTime: String? = null

    /** 操作人  */
    var ip: String? = null

    /** 操作人  */
    var spendTime: String? = null

    /** 操作人  */
    var className: String? = null

    /** 操作人  */
    var method: String? = null

    /** 操作人  */
    var params: String? = null

    /** 操作人  */
    var exDesc: String? = null

    /** 操作人  */
    var loginUser: String? = null
}