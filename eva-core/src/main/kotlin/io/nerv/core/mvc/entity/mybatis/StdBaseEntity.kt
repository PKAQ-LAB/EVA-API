package io.nerv.core.mvc.entity.mybatis

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import io.swagger.annotations.ApiModelProperty
import lombok.Data
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 实体类基类，用于存放公共属性
 * @author: S.PKAQ
 * @Datetime: 2018/3/13 22:48
 */
@Data
abstract class StdBaseEntity : Entity, Serializable {
    @TableId(type = IdType.ASSIGN_UUID)
    private val id: String? = null

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    private val createBy: String? = null

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private val gmtCreate: LocalDateTime? = null

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private val modifyBy: String? = null

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private val gmtModify: LocalDateTime? = null

    @ApiModelProperty("备注")
    private val remark: String? = null
}