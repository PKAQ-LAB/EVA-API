package io.nerv.core.mvc.util

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import lombok.Data
import java.io.Serializable
import java.util.*

/**
 * 用于接收数组参数的虚拟对象
 * Datetime: 2017-06-13 22:06
 * @author S.PKAQ
 */
@ApiModel("list接收对象")
open class SingleArray<T> : Serializable {
    @ApiModelProperty("对象数组")
    val param: ArrayList<T>? = null

    @ApiModelProperty("状态字段")
    val status: String? = null
}