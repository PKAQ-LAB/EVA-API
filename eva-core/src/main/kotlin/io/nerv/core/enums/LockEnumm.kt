package io.nerv.core.enums

/**
 * @author: S.PKAQ
 */
enum class LockEnumm(var value: String, var code: String) : BizCode {
    /**
     * 锁定
     */
    LOCK("锁定", "0001"),
    UNLOCK("正常", "0000");

    override fun getName(): String? {
        return this.value
    }

    override fun getIndex(): String? {
        return this.code
    }
}