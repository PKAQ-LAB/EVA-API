package io.nerv.core.mvc.util

import com.baomidou.mybatisplus.core.metadata.IPage
import io.nerv.core.enums.BizCode
import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.util.ReflectHelper
import io.nerv.exception.ReflectException

open class Response {
   var code: String? = null
   var success = false
   var message: String? = null
   var data: Any? = null

    constructor() {}
    constructor(success: Boolean, data: Any?) {
        this.data = data
        this.success = success
    }

    /**
     * 响应成功
     * @return
     */
    fun success(): Response {
        success = true
        code = BizCodeEnum.OPERATE_SUCCESS.getIndex()
        return this
    }

    /**
     * 响应成功
     * @param data
     * @return
     */
    fun success(data: Any?): Response {
        this.data = data
        success = true
        code = BizCodeEnum.OPERATE_SUCCESS.getIndex()
        return this
    }

    /**
     * 响应成功
     * @param data
     * @return
     */
    fun success(data: Any?, msg: String?): Response {
        this.data = data
        success = true
        message = msg
        code = BizCodeEnum.OPERATE_SUCCESS.getIndex()
        return this
    }

    /**
     * 响应成功
     * @param data
     * @return
     */
    fun success(data: Any?, msg: BizCode): Response {
        this.data = data
        success = true
        message = msg.getName()
        code = msg.getIndex()
        return this
    }

    /**
     * 响应成功
     * @param data
     * @return
     */
    fun success(data: Any?, msg: String?, code: String?): Response {
        this.data = data
        success = true
        message = msg
        this.code = code
        return this
    }

    /**
     * 失败响应，自定义响应码和消息
     * @param code
     * @return
     */
    fun failure(code: String?, message: String?): Response {
        success = false
        this.code = code
        this.message = message
        return this
    }

    /**
     * 失败响应，自定义响应码和消息
     * @param errorCodeEnum
     * @return
     */
    fun failure(errorCodeEnum: BizCode): Response {
        success = false
        code = errorCodeEnum.getIndex()
        message = String.format("[%s] %s",
                errorCodeEnum.getIndex(), errorCodeEnum.getName())
        return this
    }

    /**
     * 响应失败
     * @param data
     * @return
     */
    fun failure(code: String?, message: String?, data: Any?): Response {
        this.data = data
        this.message = message
        success = false
        this.code = code
        return this
    }

    /**
     * 对data对象里对应的属性置空
     * @param values 需要为空的属性名
     * @return
     */
    fun exclude(data: Any?, values: Array<String>): Response {
        var thisObject = data
        if (data != null) {
            for (value in values) {

                //如果对象是Ipage对象，则fatherObject等于实际传给前台的对象即records集合。
                if (data is IPage<*>) {
                    thisObject = data.records
                }
                val splitValue = value.split("\\.".toRegex()).toTypedArray()

                //thisObject的直接属性的话，判断什么类型然后直接置位空
                if (splitValue.size == 1) {
                    //对thisObject的splitValue[0]属性赋空值
                    ReflectHelper.setNull(thisObject, splitValue[0])
                } else {
                    //链式属性的话，循环每一阶层的属性名
                    for (i in splitValue.indices) {
                        val stepValue = splitValue[i]

                        //得到当前循环的子集的所有下层子集
                        val begin = value.indexOf(".", i + 1)
                        val sValue = arrayOf(value.substring(begin + 1, value.length))

                        //对当前对象判断
                        //如果是数组或集合,则循环属性值，把接下去的阶层属性置空
                        if (thisObject!!.javaClass.isArray || thisObject is Collection<*>) {
                            for (obj in (thisObject as Collection<*>?)!!) {
                                val field = ReflectHelper.getField(obj, stepValue)
                                if (field != null) {
                                    field.isAccessible = true
                                    try {
                                        exclude(field[obj], sValue)
                                    } catch (e: IllegalAccessException) {
                                        throw ReflectException("得到" + obj?.javaClass + "对象的" + stepValue + "属性值失败")
                                    }
                                }
                            }
                            break
                        } else if (thisObject is Map<*, *>) {
                            //如果是map集合的话，则得到map对应的key(下一阶层的属性名)的value对象，然后递归该对象
                            exclude(thisObject[stepValue], sValue)
                            break
                        } else {
                            val field = ReflectHelper.getField(thisObject, stepValue)
                            if (field != null) {
                                field.isAccessible = true
                                try {
                                    exclude(field[thisObject], sValue)
                                } catch (e: IllegalAccessException) {
                                    throw ReflectException("得到" + thisObject.javaClass + "对象的" + stepValue + "属性值失败")
                                }
                            }
                        }
                    }
                }
            }
        }
        this.data = thisObject;
        return this
    }
}