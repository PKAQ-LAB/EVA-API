package io.nerv.core.enums

/**
 * @author: S.PKAQ
 */
enum class DataPermissionEnumm(var value: String, var code: String) : BizCode {
    /**
     * 可用
     */
    ALL("全部权限", "0000"),
    DEPT_ONLY_LIMIT("仅本部门", "0001"),
    DEPT_AND_CHILDREN_LIMIT("本人所属部门及下属部门", "0002"),
    DEPT_LIMIT("指定部门", "0003"),
    CREATOR_LIMIT("本人创建或修改", "0005");

    override fun getName(): String? {
        return this.value
    }

    override fun getIndex(): String? {
        return this.code
    }
}