package io.nerv.core.enums

/**
 * @author: S.PKAQ
 */
enum class ResponseEnumm(var value: String)  : BizCode{
    /**
     * 可用
     */
    OPERATE_SUCCESS("操作成功"),
    SAVE_SUCCESS("保存成功"),
    EDIT_SUCCESS("编辑成功"),
    DELETE_SUCCESS("删除成功"),
    QUERY_SUCCESS("查询成功"),
    NULL_MSG(""),
    OPERATE_FAILED("操作失败");

    override fun getName(): String? {
        return this.value
    }

    override fun getIndex(): String? {
        return this.value
    }
}